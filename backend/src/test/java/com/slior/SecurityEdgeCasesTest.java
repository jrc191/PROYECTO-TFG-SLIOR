package com.slior;

import com.slior.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityEdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void whenAccessProtectedNoToken_then401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/data-export"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Token inválido o ausente"));
    }

    @Test
    public void whenLoginInvalidCredentials_then401() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "wrongpass");
        
        mockMvc.perform(post("/auth/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Email o contraseña incorrectos"));
    }

    @Test
    public void whenCorrelationIdRequested_thenResponseHasIt() throws Exception {
        mockMvc.perform(get("/health")) // Un endpoint público
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    public void whenRateLimitExceeded_then429() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "pass");
        String content = objectMapper.writeValueAsString(request);

        // El límite para login es 5 por minuto. Hacemos 6.
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/v1/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));
        }

        mockMvc.perform(post("/auth/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isTooManyRequests());
    }
}
