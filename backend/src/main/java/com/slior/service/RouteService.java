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
import com.slior.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.slior.exception.RouteNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

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
        return RouteResponse.from(saved);
    }

    public List<RouteResponse> getRoutesForRepartidor(UUID repartidorId) {
        return routeRepository.findByRepartidorIdAndIsDeletedFalse(repartidorId)
                .stream()
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

        // Para simplificar la actualización, eliminamos las paradas actuales y creamos las nuevas
        // En una app real se podría hacer un "diff", pero para un TFG recrearlas es más limpio y evita bugs de estado.
        route.getStops().clear();
        
        List<Stop> nuevasParadas = new ArrayList<>();
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
            nuevasParadas.add(stop);
        }
        route.getStops().addAll(nuevasParadas);

        Route updated = routeRepository.save(route);
        return RouteResponse.from(updated);
    }
}

