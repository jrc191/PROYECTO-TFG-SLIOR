package com.slior.controller;

import com.slior.dto.route.CreateRouteRequest;
import com.slior.dto.route.RouteResponse;
import com.slior.dto.route.UpdateRouteRequest;
import com.slior.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.slior.dto.route.OptimizeRouteRequest;
import com.slior.service.RouteOptimizationService;
import com.slior.service.LabelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteOptimizationService routeOptimizationService;
    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        RouteResponse response = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/repartidor")
    public ResponseEntity<List<RouteResponse>> getRoutesByRepartidorQuery(@RequestParam UUID repartidorId) {
        return ResponseEntity.ok(routeService.getRoutesForRepartidor(repartidorId));
    }

    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<List<RouteResponse>> getRoutesByRepartidor(@PathVariable UUID repartidorId) {
        return ResponseEntity.ok(routeService.getRoutesForRepartidor(repartidorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable UUID id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable UUID id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @PostMapping("/{id}/optimize")
    public ResponseEntity<RouteResponse> optimizeRoute(
            @PathVariable UUID id,
            @Valid @RequestBody OptimizeRouteRequest request) {
        return ResponseEntity.ok(routeOptimizationService.optimizarRuta(id, request));
    }

    @PatchMapping("/stops/{stopId}/status")
    public ResponseEntity<RouteResponse> updateStopStatus(
            @PathVariable UUID stopId,
            @RequestParam String status) {
        com.slior.model.enums.StopStatus newStatus = com.slior.model.enums.StopStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(routeService.updateStopStatus(stopId, newStatus));
    }

    @GetMapping("/stops/{stopId}/label")
    public ResponseEntity<String> saveStopLabel(@PathVariable UUID stopId) throws Exception {
        String filePath = labelService.saveLabelPdfToDisk(stopId);
        return ResponseEntity.ok("PDF guardado en: " + filePath);
    }
}
