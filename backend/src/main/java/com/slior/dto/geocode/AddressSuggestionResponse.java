package com.slior.dto.geocode;

public record AddressSuggestionResponse(
        String displayName,
        Double latitude,
        Double longitude
) {
}

