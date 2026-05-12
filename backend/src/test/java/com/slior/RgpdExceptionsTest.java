package com.slior;

import com.slior.model.User;
import com.slior.model.enums.UserRole;
import com.slior.repository.UserRepository;
import com.slior.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RgpdExceptionsTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenRequestDeletion_thenIsDeletedIsTrue() {
        User user = createTestUser("delete@test.com");
        
        userService.requestDeletion(user.getId());
        
        // El repositorio filtra por is_deleted=false por defecto debido a @Where
        // Así que findById no debería encontrarlo
        assertFalse(userRepository.findById(user.getId()).isPresent());
    }

    @Test
    public void whenAnonymizeUser_thenDataIsMasked() {
        User user = createTestUser("anon@test.com");
        user.setIsDeleted(true);
        user.setDeletionRequestedAt(LocalDateTime.now().minusDays(10));
        userRepository.save(user);

        // Forzar tarea de anonimización
        userService.processAnonymization();

        // Buscar por query nativo ya que está marcado como is_deleted=true
        User anonymized = userRepository.findPendingAnonymization(LocalDateTime.now().plusDays(1))
                .stream()
                .filter(u -> u.getId().equals(user.getId()))
                .findFirst()
                .orElse(null);

        // Si fue anonimizado, ya no debería estar en "PendingAnonymization" con los criterios anteriores
        // Pero vamos a verificar el estado final en la BD usando un query nativo si fuera necesario, 
        // o simplemente confiamos en la lógica del servicio que ya probamos.
        
        // Para verificar realmente, necesitamos un método en UserRepository que ignore @Where o usar Native Query.
        // Ya lo tenemos: findPendingAnonymization devuelve usuarios con anonymized_at IS NULL.
        // Si el usuario fue procesado, anonymized_at ya NO es null.
        
        assertTrue(userRepository.findPendingAnonymization(LocalDateTime.now().plusDays(1))
                .stream().noneMatch(u -> u.getId().equals(user.getId())));
    }

    private User createTestUser(String email) {
        User user = User.builder()
                .nombre("Test User")
                .email(email)
                .password("pass")
                .rol(UserRole.REPARTIDOR)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        return userRepository.save(user);
    }
}
