package com.slior.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slior.dto.route.OptimizeRouteRequest;
import com.slior.dto.route.RouteResponse;
import com.slior.exception.RouteNotFoundException;
import com.slior.model.Route;
import com.slior.model.Stop;
import com.slior.model.enums.VehicleType;
import com.slior.repository.RouteRepository;
import com.slior.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(RouteOptimizationService.class);
    private final RouteRepository routeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving/";
    
    // Factor de corrección: OSRM es conservador (~80km/h). 
    // Multiplicando por 0.8 bajamos un 20% el tiempo, acercándonos a la media de 100-110km/h de Google en autovía.
    private static final double OSRM_SPEED_CORRECTION = 0.87;

    @Transactional
    public RouteResponse optimizarRuta(UUID routeId, OptimizeRouteRequest request) {
        Route route = routeRepository.findByIdAndIsDeletedFalse(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId.toString()));

        List<Stop> todasLasParadas = route.getStops().stream()
                .filter(s -> !s.isDeleted())
                .collect(Collectors.toList());

        if (todasLasParadas.isEmpty()) {
            return RouteResponse.from(route);
        }

        // 1. Separar paradas: Las ya entregadas no se optimizan, se quedan al principio en su orden actual
        List<Stop> entregadas = todasLasParadas.stream()
                .filter(s -> s.getStatus() == com.slior.model.enums.StopStatus.ENTREGADO)
                .sorted(java.util.Comparator.comparing(Stop::getOrdenVisita))
                .collect(Collectors.toList());

        List<Stop> pendientes = todasLasParadas.stream()
                .filter(s -> s.getStatus() != com.slior.model.enums.StopStatus.ENTREGADO)
                .collect(Collectors.toCollection(ArrayList::new));

        // 2. Optimizar solo las pendientes desde la ubicación actual o última entregada
        double startLat = request.puntoInicioLat();
        double startLon = request.puntoInicioLon();

        if (!entregadas.isEmpty()) {
            Stop ultimaEntregada = entregadas.get(entregadas.size() - 1);
            startLat = ultimaEntregada.getLatitud();
            startLon = ultimaEntregada.getLongitud();
        }

        List<Stop> pendientesOrdenadas = nearestNeighbor(pendientes, startLat, startLon);

        // 3. Recomponer la ruta completa: Entregadas + Pendientes Optimizadas
        List<Stop> rutaFinal = new ArrayList<>(entregadas);
        rutaFinal.addAll(pendientesOrdenadas);

        // 4. Actualizar el orden de visita global
        for (int i = 0; i < rutaFinal.size(); i++) {
            rutaFinal.get(i).setOrdenVisita(i + 1);
        }

        // 5. Calcular métricas reales con OSRM (desde el punto de inicio real de la petición)
        OSRMResult metrics = calcularMetricasReales(
                request.puntoInicioLat(),
                request.puntoInicioLon(),
                rutaFinal
        );

        if (metrics != null) {
            route.setDistanciaTotal(metrics.distanceKm());
            route.setTiempoEstimado(ajustarTiempoPorVehiculo(metrics.durationMinutes(), request.vehicleType()));
        } else {
            // Fallback a Haversine
            double distanciaKm = calcularDistanciaTotalHaversine(
                    request.puntoInicioLat(),
                    request.puntoInicioLon(),
                    rutaFinal
            );
            route.setDistanciaTotal(Math.round(distanciaKm * 100.0) / 100.0);
            route.setTiempoEstimado(calcularTiempoEstimadoFallback(distanciaKm, request.vehicleType()));
        }

        return RouteResponse.from(routeRepository.save(route));
    }

    private OSRMResult calcularMetricasReales(double latInicio, double lonInicio, List<Stop> paradas) {
        try {
            // OSRM usa formato lon,lat separados por ;
            StringBuilder coords = new StringBuilder();
            coords.append(lonInicio).append(",").append(latInicio);

            for (Stop p : paradas) {
                coords.append(";").append(p.getLongitud()).append(",").append(p.getLatitud());
            }

            String url = OSRM_URL + coords.toString() + "?overview=false";
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            if (!"Ok".equals(root.path("code").asText())) return null;

            JsonNode routeNode = root.path("routes").get(0);
            double distanceMeters = routeNode.path("distance").asDouble();
            double durationSeconds = routeNode.path("duration").asDouble();

            // Aplicar factor de corrección para compensar la estimación conservadora de OSRM
            int durationMinutes = (int) Math.ceil((durationSeconds / 60.0) * OSRM_SPEED_CORRECTION);

            return new OSRMResult(
                Math.round((distanceMeters / 1000.0) * 100.0) / 100.0,
                durationMinutes
            );
        } catch (Exception e) {
            log.error("Error consultando OSRM: {}", e.getMessage());
            return null;
        }
    }

    private int ajustarTiempoPorVehiculo(int minutosBase, VehicleType type) {
        // Temporalmente devolvemos el tiempo base de OSRM sin extras
        return minutosBase;
    }

    private int calcularTiempoEstimadoFallback(double distanciaKm, VehicleType vehicleType) {
        // Fallback simple: 55km/h media sin multiplicadores
        return (int) Math.ceil((distanciaKm / 55.0) * 60);
    }

    private record OSRMResult(double distanceKm, int durationMinutes) {}

    // Nearest Neighbor: desde el punto actual, ir siempre a la parada más cercana
    private List<Stop> nearestNeighbor(List<Stop> paradas, double latActual, double lonActual) {
        List<Stop> pendientes = new ArrayList<>(paradas);
        List<Stop> resultado = new ArrayList<>();
        double currentLat = latActual;
        double currentLon = lonActual;

        while (!pendientes.isEmpty()) {
            Stop masCercana = null;
            double minDistancia = Double.MAX_VALUE;

            for (Stop parada : pendientes) {
                double d = HaversineUtil.calculateDistance(
                        currentLat, currentLon,
                        parada.getLatitud(), parada.getLongitud()
                );
                if (d < minDistancia) {
                    minDistancia = d;
                    masCercana = parada;
                }
            }

            resultado.add(masCercana);
            pendientes.remove(masCercana);
            currentLat = masCercana.getLatitud();
            currentLon = masCercana.getLongitud();
        }

        return resultado;
    }

    private double calcularDistanciaTotalHaversine(double latInicio, double lonInicio, List<Stop> paradas)
    {
        double total = 0.0;
        double latPrev = latInicio;
        double lonPrev = lonInicio;

        for (Stop parada : paradas) {
            total += HaversineUtil.calculateDistance(latPrev, lonPrev,
                    parada.getLatitud(), parada.getLongitud());
            latPrev = parada.getLatitud();
            lonPrev = parada.getLongitud();
        }

        return total;
    }
}
