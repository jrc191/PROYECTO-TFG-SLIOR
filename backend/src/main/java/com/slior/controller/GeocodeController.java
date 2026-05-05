package com.slior.controller;

import com.slior.dto.geocode.AddressSuggestionResponse;
import com.slior.service.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/geocode")
@RequiredArgsConstructor
public class GeocodeController {

    private final GeocodeService geocodeService;

    @GetMapping("/search")
    public ResponseEntity<List<AddressSuggestionResponse>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(geocodeService.searchAddresses(query));
    }
}

