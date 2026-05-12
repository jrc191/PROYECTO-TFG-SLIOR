package com.slior;

import com.slior.service.LabelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class LabelAsyncTest {

    @Autowired
    private LabelService labelService;

    @Test
    public void whenGenerateLabelNonExistentStop_thenNoExceptionThrownAsync() {
        // El método async no debe propagar la excepción al llamador
        assertDoesNotThrow(() -> {
            labelService.generateLabelsAsync(Collections.singletonList(UUID.randomUUID()));
        });
    }
}
