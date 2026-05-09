package com.slior.service;

import com.slior.model.AuditLog;
import com.slior.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Servicio para gestionar los logs de auditoría.
 * Centraliza la lógica de hashing de IP y registro en BD.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private static final String MDC_KEY = "requestId";

    /**
     * Registra una acción en el log de auditoría.
     * @param userId ID del usuario (puede ser null)
     * @param action Acción realizada (LOGIN, EXPORT_DATA, etc.)
     * @param entity Entidad afectada (User, Route, Stop)
     */
    @Transactional
    public void log(UUID userId, String action, String entity) {
        HttpServletRequest request = getCurrentRequest();
        String ip = resolveIp(request);
        String ipHash = hashIp(ip);
        String requestId = MDC.get(MDC_KEY);

        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entity(entity)
                .ipHash(ipHash)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();

        auditLogRepository.save(log);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String hashIp(String ip) {
        if (ip == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // fallback en caso extremo, aunque SHA-256 es estándar en Java
            return "HASH_ERROR";
        }
    }
}

