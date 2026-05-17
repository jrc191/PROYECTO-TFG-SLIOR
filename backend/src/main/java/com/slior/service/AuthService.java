package com.slior.service;

import com.slior.dto.auth.*;
import com.slior.exception.EmailAlreadyExistsException;
import com.slior.exception.InvalidCredentialsException;
import com.slior.exception.UserNotFoundException;
import com.slior.model.PasswordResetToken;
import com.slior.model.User;
import com.slior.model.enums.VehicleType;
import com.slior.repository.PasswordResetTokenRepository;
import com.slior.repository.UserRepository;
import com.slior.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Lógica de negocio para autenticación.
 * Gestiona registro y login de usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    /**
     * Registra un nuevo usuario en el sistema.
     * Hashea el password con BCrypt antes de persistir.
     * Usa el vehicleType proporcionado en la solicitud.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .nombre(request.nombre())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .vehicleType(request.vehicleType())
                .build();

        User saved = userRepository.save(user);
        
        // Registrar en auditoría
        auditService.log(saved.getId(), "REGISTER", "User");
        
        String token = generateTokenForEmail(saved.getEmail());

        return new AuthResponse(token, "Bearer", saved.getId(),
                saved.getNombre(), saved.getEmail(), saved.getRol(), getVehicleType(saved),
                saved.getConsentimientoNotificaciones());
    }

    /**
     * Autentica un usuario existente y retorna un JWT.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Registrar en auditoría
        auditService.log(user.getId(), "LOGIN", "User");

        String token = generateTokenForEmail(user.getEmail());

        return new AuthResponse(token, "Bearer", user.getId(),
                user.getNombre(), user.getEmail(), user.getRol(), getVehicleType(user),
                user.getConsentimientoNotificaciones());
    }

    /**
     * Genera un código de restablecimiento y "envía" un email mock.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + request.email()));

        // Limpiar tokens anteriores
        tokenRepository.deleteByEmail(user.getEmail());

        // Generar código de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(user.getEmail())
                .token(code)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // Expira en 15 min
                .build();

        tokenRepository.save(resetToken);

        // EXTRAER CÓDIGO A ARCHIVO DE LOGS
        try {
            File directory = new File("logs/reset-password");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File logFile = new File(directory, user.getEmail() + ".txt");
            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write("Email: " + user.getEmail() + "\n");
                writer.write("Código: " + code + "\n");
                writer.write("Fecha: " + LocalDateTime.now() + "\n");
            }
            log.info("Código de restablecimiento guardado en: {}", logFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error al guardar el código de restablecimiento en archivo", e);
        }

        // MOCK EMAIL
        log.info("************************************************************");
        log.info("MOCK EMAIL SENT TO: {}", user.getEmail());
        log.info("SUBJECT: Restablecimiento de contraseña - SLIOR");
        log.info("BODY: Tu código de seguridad es: {}", code);
        log.info("************************************************************");
        
        auditService.log(user.getId(), "FORGOT_PASSWORD_REQUEST", "User");
    }

    /**
     * Verifica el código y cambia la contraseña.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByEmailAndToken(request.email(), request.token())
                .orElseThrow(() -> new IllegalArgumentException("Código de verificación inválido o email incorrecto"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("El código ha expirado");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Borrar el token usado
        tokenRepository.delete(resetToken);

        auditService.log(user.getId(), "PASSWORD_RESET_SUCCESS", "User");
        log.info("Contraseña actualizada con éxito para el usuario: {}", user.getEmail());
    }

    /**
     * Permite a un usuario autenticado cambiar su contraseña.
     */
    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        auditService.log(user.getId(), "PASSWORD_UPDATE_SUCCESS", "User");
        log.info("Contraseña actualizada por el usuario: {}", user.getEmail());
    }

    /**
     * Retorna el vehicleType del usuario, o VAN si es null.
     * Esto maneja usuarios existentes que no tenían este campo.
     */
    private VehicleType getVehicleType(User user) {
        return user.getVehicleType() != null ? user.getVehicleType() : VehicleType.VAN;
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));
    }

    private String generateTokenForEmail(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails);
    }
}
