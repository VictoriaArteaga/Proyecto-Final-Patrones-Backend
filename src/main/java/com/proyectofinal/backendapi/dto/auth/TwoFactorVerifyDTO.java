package com.proyectofinal.backendapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorVerifyDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String code; // el código de 6 dígitos
}
