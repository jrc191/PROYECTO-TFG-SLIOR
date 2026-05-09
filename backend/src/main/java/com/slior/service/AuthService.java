package com.slior.service;

import com.slior.dto.auth.AuthResponse;
import com.slior.dto.auth.LoginRequest;
import com.slior.dto.auth.RegisterRequest;
import com.slior.exception.EmailAlreadyExistsException;
import com.slior.exception.InvalidCredentialsException;
import com.slior.model.User;
import com.slior.model.enums.VehicleType;
import com.slior.repository.UserRepository;
import com.slior.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio para autenticación.
 * Gestiona registro y login de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
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
                saved.getNombre(), saved.getEmail(), saved.getRol(), getVehicleType(saved));
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
                user.getNombre(), user.getEmail(), user.getRol(), getVehicleType(user));
    }

    /**
     * Retorna el vehicleType del usuario, o VAN si es null.
     * Esto maneja usuarios existentes que no tenían este campo.
     */
    private VehicleType getVehicleType(User user) {
        return user.getVehicleType() != null ? user.getVehicleType() : VehicleType.VAN;
    }

    private String generateTokenForEmail(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtUtil.generateToken(userDetails);
    }
}
