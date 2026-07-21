package com.velvet.sakura.dto.response;

import java.time.LocalDateTime;

import com.velvet.sakura.entity.DeckType;

public record ReadingResponse(
        Long id,
        Long userId,
        LocalDateTime date,
        String name,
        Long pastCardId,
        Long presentCardId,
        Long futureCardId,
        DeckType deckType,
        String question,
        String interpretation
) {}