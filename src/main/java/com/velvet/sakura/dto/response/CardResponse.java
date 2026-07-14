package com.velvet.sakura.dto.response;

public record CardResponse(
        Long id,
        String code,
        String spanishName,
        String meaning,
        String cardImageUrl,
        String reverseImageUrl
) {}