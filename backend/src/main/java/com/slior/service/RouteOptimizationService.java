package com.slior.service;

import com.slior.dto.route.OptimizeRouteRequest;
import com.slior.dto.route.RouteResponse;
import com.slior.exception.RouteNotFoundException;
import com.slior.model.Route;
import com.slior.model.Stop;
import com.slior.repository.RouteRepository;
import com.slior.util.HaversineUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    // Velocidad media urbana para calcular tiempo estimado
    private static final double VELOCIDAD_MEDIA_KMH = 30.0;

    private final RouteRepository routeRepository;

    @Transactional
    public RouteResponse optimizarRuta(UUID routeId, OptimizeRouteRequest request) {
        Route route = routeRepository.findByIdAndIsDeletedFalse(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId.toString()));

        List<Stop> paradas = route.getStops().stream()
                .filter(s -> !s.isDeleted())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        if (paradas.isEmpty()) {
            return RouteResponse.from(route);
        }

        // 1. Aplicar algoritmo Nearest Neighbor
        List<Stop> paradasOrdenadas = nearestNeighbor(
                paradas,
                request.puntoInicioLat(),
                request.puntoInicioLon()
        );

        // 2. Actualizar el orden de visita en cada parada
        for (int i = 0; i < paradasOrdenadas.size(); i++) {
            paradasOrdenadas.get(i).setOrdenVisita(i + 1);
        }

        // 3. Calcular distancia total y tiempo estimado
        double distanciaKm = calcularDistanciaTotal(
                request.puntoInicioLat(),
                request.puntoInicioLon(),
                paradasOrdenadas
        );
        route.setDistanciaTotal(Math.round(distanciaKm * 100.0) / 100.0);
        route.setTiempoEstimado((int) Math.ceil((distanciaKm / VELOCIDAD_MEDIA_KMH) * 60));

        return RouteResponse.from(routeRepository.save(route));
    }

    // Nearest Neighbor: desde el punto actual, ir siempre a la parada más cercana
    private List<Stop> nearestNeighbor(List<Stop> paradas, double latInicio, double lonInicio) {
        List<Stop> pendientes = new ArrayList<>(paradas);
        List<Stop> resultado = new ArrayList<>();
        double latActual = latInicio;
        double lonActual = lonInicio;

        while (!pendientes.isEmpty()) {
            Stop masCercana = null;
            double minDistancia = Double.MAX_VALUE;

            for (Stop parada : pendientes) {
                double d = HaversineUtil.calculateDistance(
                        latActual, lonActual,
                        parada.getLatitud(), parada.getLongitud()
                );
                if (d < minDistancia) {
                    minDistancia = d;
                    masCercana = parada;
                }
            }

            resultado.add(masCercana);
            pendientes.remove(masCercana);
            latActual = masCercana.getLatitud();
            lonActual = masCercana.getLongitud();
        }

        return resultado;
    }

    private double calcularDistanciaTotal(double latInicio, double lonInicio, List<Stop> paradas)
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