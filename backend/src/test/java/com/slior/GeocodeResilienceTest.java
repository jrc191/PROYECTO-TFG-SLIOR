package com.slior;

import com.slior.dto.geocode.AddressSuggestionResponse;
import com.slior.service.GeocodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeocodeResilienceTest {

    @Autowired
    private GeocodeService geocodeService;

    @Test
    public void whenQueryTooShort_thenEmptyList() {
        List<AddressSuggestionResponse> results = geocodeService.searchAddresses("ab");
        assertTrue(results.isEmpty());
    }

    @Test
    public void whenQueryWithSpecialChars_thenSanitized() {
        // No debería explotar y debería retornar algo (o vacío si no encuentra nada)
        assertDoesNotThrow(() -> {
            geocodeService.searchAddresses("Calle Falsa 123 !!!! $$$$");
        });
    }

    @Test
    public void whenNominatimFails_thenFallbackToLocalOrEmpty() {
        // En un entorno de test sin internet o con Nominatim mockeado
        // aquí verificaríamos que el sistema no lanza excepción.
        List<AddressSuggestionResponse> results = geocodeService.searchAddresses("Direccion Inexistente 999999");
        assertNotNull(results);
    }
}
