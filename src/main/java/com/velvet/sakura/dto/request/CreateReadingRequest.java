package com.velvet.sakura.dto.request;

import com.velvet.sakura.entity.DeckType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReadingRequest {
    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    @NotNull
    private Long pastCardId;

    @NotNull
    private Long presentCardId;

    @NotNull
    private Long futureCardId;

    @NotNull
    private DeckType deckType;
}
