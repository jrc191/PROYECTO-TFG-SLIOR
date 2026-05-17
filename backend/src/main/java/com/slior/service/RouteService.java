package com.slior.service;

import com.slior.dto.route.RouteResponse;
import com.slior.dto.route.*;
import com.slior.model.Route;
import com.slior.model.Stop;
import com.slior.model.User;
import com.slior.exception.UserNotFoundException;
import com.slior.model.enums.RouteStatus;
import com.slior.model.enums.StopStatus;
import com.slior.repository.RouteRepository;
import com.slior.repository.StopRepository;
import com.slior.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.slior.exception.RouteNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final StopRepository stopRepository;
    private final LabelService labelService;

    @Transactional
    public RouteResponse createRoute(CreateRouteRequest request) {
        User repartidor = userRepository.findById(request.repartidorId())
                .orElseThrow(() -> new UserNotFoundException(request.repartidorId().toString()));

        Route route = new Route();
        route.setNombre(request.nombre());
        route.setFechaPlanificada(request.fechaPlanificada());
        route.setRepartidor(repartidor);
        route.setStatus(RouteStatus.PLANIFICADA);
        route.setNotas(request.notas());

        List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < request.paradas().size(); i++) {
            StopRequest sr = request.paradas().get(i);
            Stop stop = new Stop();
            stop.setDireccion(sr.direccion());
            stop.setLatitud(sr.latitud());
            stop.setLongitud(sr.longitud());
            stop.setDestinatario(sr.destinatario());
            stop.setTelefonoDestinatario(sr.telefonoDestinatario());
            stop.setNotas(sr.notas());
            stop.setOrdenVisita(i + 1);
            stop.setStatus(StopStatus.PENDIENTE);
            stop.setRoute(route);
            stops.add(stop);
        }
        route.setStops(stops);

        Route saved = routeRepository.save(route);

        // Generar PDFs automáticamente en segundo plano para no bloquear la respuesta
        // IMPORTANTE: Se usa TransactionSynchronizationManager para asegurar que el hilo async
        // vea los datos recién creados (después del commit).
        List<UUID> stopIds = saved.getStops().stream().map(Stop::getId).toList();
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    labelService.generateLabelsAsync(stopIds);
                }
            });
        } else {
            labelService.generateLabelsAsync(stopIds);
        }

        return RouteResponse.from(saved);
    }

    public List<RouteResponse> getRoutesForRepartidor(UUID repartidorId) {
        List<Route> routes = routeRepository.findByRepartidorIdAndIsDeletedFalse(repartidorId);
        
        return routes.stream()
                .map(RouteResponse::from)
                .toList();
    }

    public RouteResponse getRouteById(UUID id) {
        Route route = routeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));
        return RouteResponse.from(route);
    }

    public void deleteRoute(UUID id) {
        Route route = routeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));
        route.setDeleted(true);
        routeRepository.save(route);
    }

    @Transactional
    public RouteResponse updateRoute(UUID id, UpdateRouteRequest request) {
        Route route = routeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RouteNotFoundException(id.toString()));

        route.setNombre(request.nombre());
        route.setFechaPlanificada(request.fechaPlanificada());
        route.setNotas(request.notas());

        // Mapear paradas existentes para identificar cuáles se quedan
        Map<UUID, Stop> existingStopsMap = route.getStops().stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(Stop::getId, s -> s));
        
        // Limpiamos la lista para reconstruirla con el nuevo orden,
        // pero manteniendo las referencias a los objetos que ya existen.
        List<Stop> currentStops = route.getStops();
        currentStops.clear();
        
        for (int i = 0; i < request.paradas().size(); i++) {
            StopRequest sr = request.paradas().get(i);
            Stop stop;

            if (sr.id() != null && existingStopsMap.containsKey(sr.id())) {
                // Actualizar parada existente
                stop = existingStopsMap.get(sr.id());
                stop.setDireccion(sr.direccion());
                stop.setLatitud(sr.latitud());
                stop.setLongitud(sr.longitud());
                stop.setDestinatario(sr.destinatario());
                stop.setTelefonoDestinatario(sr.telefonoDestinatario());
                stop.setNotas(sr.notas());
                stop.setOrdenVisita(i + 1);
            } else {
                // Crear parada nueva
                stop = new Stop();
                stop.setDireccion(sr.direccion());
                stop.setLatitud(sr.latitud());
                stop.setLongitud(sr.longitud());
                stop.setDestinatario(sr.destinatario());
                stop.setTelefonoDestinatario(sr.telefonoDestinatario());
                stop.setNotas(sr.notas());
                stop.setOrdenVisita(i + 1);
                stop.setStatus(StopStatus.PENDIENTE);
                stop.setRoute(route);
            }
            currentStops.add(stop);
        }

        Route updated = routeRepository.save(route);

        // Generar/Actualizar PDFs para TODAS las paradas de la ruta
        // Esto asegura que si ha cambiado el orden de visita (#1, #2...), la etiqueta lo refleje.
        List<UUID> stopIds = updated.getStops().stream().map(Stop::getId).toList();

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    labelService.generateLabelsAsync(stopIds);
                }
            });
        } else {
            labelService.generateLabelsAsync(stopIds);
        }

        return RouteResponse.from(updated);
    }

    @Transactional
    public RouteResponse updateStopStatus(UUID stopId, StopStatus newStatus) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new com.slior.exception.StopNotFoundException(stopId.toString()));
        
        stop.setStatus(newStatus);
        if (newStatus == StopStatus.ENTREGADO) {
            stop.setEntregadoEn(java.time.LocalDateTime.now());
        } else {
            stop.setEntregadoEn(null);
        }
        
        stopRepository.save(stop);
        
        // Return the updated route
        return RouteResponse.from(stop.getRoute());
    }
}

