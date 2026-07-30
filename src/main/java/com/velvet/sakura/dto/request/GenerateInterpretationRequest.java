package com.velvet.sakura.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateInterpretationRequest {
    private String question;

    @NotNull
    private Long pastCardId;

    @NotNull
    private Long presentCardId;

    @NotNull
    private Long futureCardId;
}