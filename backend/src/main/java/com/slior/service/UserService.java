package com.slior.service;

import com.slior.dto.user.UserDataExportDto;
import com.slior.exception.UserNotFoundException;
import com.slior.model.Route;
import com.slior.model.Stop;
import com.slior.model.User;
import com.slior.repository.RouteRepository;
import com.slior.repository.StopRepository;
import com.slior.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public UserDataExportDto exportUserData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        List<Route> routes = routeRepository.findByRepartidorIdAndIsDeletedFalse(userId);

        List<UserDataExportDto.RouteData> routeDataList = routes.stream().map(route -> {
            List<Stop> stops = stopRepository.findByRouteIdAndIsDeletedFalseOrderByOrdenVisitaAsc(route.getId());
            return UserDataExportDto.RouteData.builder()
                    .nombre(route.getNombre())
                    .fecha(route.getFechaPlanificada().toString())
                    .status(route.getStatus().name())
                    .stops(stops.stream().map(stop -> UserDataExportDto.StopData.builder()
                            .direccion(stop.getDireccion())
                            .destinatario(stop.getDestinatario())
                            .status(stop.getStatus().name())
                            .entregadoEn(stop.getEntregadoEn() != null ? stop.getEntregadoEn().toString() : null)
                            .build()).collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());

        auditService.log(userId, "EXPORT_DATA", "User");

        return UserDataExportDto.builder()
                .profile(UserDataExportDto.UserProfile.builder()
                        .nombre(user.getNombre())
                        .email(user.getEmail())
                        .rol(user.getRol().name())
                        .vehicleType(user.getVehicleType() != null ? user.getVehicleType().name() : "VAN")
                        .createdAt(user.getCreatedAt().toString())
                        .build())
                .routes(routeDataList)
                .build();
    }

    @Transactional
    public void requestDeletion(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        user.setIsDeleted(true);
        user.setDeletionRequestedAt(LocalDateTime.now());
        userRepository.save(user);
        
        auditService.log(userId, "DELETE_ACCOUNT_REQUEST", "User");
        log.info("User {} requested deletion. Marked as isDeleted=true", userId);
    }

    @Transactional
    public void updateLimitProcessing(UUID userId, boolean limited) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        
        user.setTratamientoLimitado(limited);
        userRepository.save(user);
        
        auditService.log(userId, limited ? "LIMIT_PROCESSING_ON" : "LIMIT_PROCESSING_OFF", "User");
    }

    @Scheduled(cron = "0 0 2 * * *") // A las 2 AM cada día
    @Transactional
    public void processAnonymization() {
        log.info("Starting scheduled anonymization task...");
        // En producción, esperaríamos 30 días. Para este prototipo, anonimizamos a los 7 días.
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        
        List<User> pending = userRepository.findPendingAnonymization(threshold);
        log.info("Found {} users pending anonymization", pending.size());

        for (User user : pending) {
            String anonId = UUID.randomUUID().toString().substring(0, 8);
            user.setEmail("anon_" + anonId + "@deleted.slior.es");
            user.setNombre("USUARIO_ELIMINADO");
            user.setPassword("ANONYMIZED_" + UUID.randomUUID());
            user.setAnonymizedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("User {} has been anonymized", user.getId());
        }
    }
}
