package com.proyectofinal.backendapi.dto.auth;
import lombok.Data;

@Data

public class TwoFactorToggleDTO {
    private String email;
    private boolean enable;
}
