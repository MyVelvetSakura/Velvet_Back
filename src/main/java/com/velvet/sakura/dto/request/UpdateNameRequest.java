package com.velvet.sakura.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNameRequest {
    @NotBlank
    private String name;
}