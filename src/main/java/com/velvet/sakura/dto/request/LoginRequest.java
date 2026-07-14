package com.velvet.sakura.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String password;
}