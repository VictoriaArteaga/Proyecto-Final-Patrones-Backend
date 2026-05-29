package com.proyectofinal.backendapi.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Body para guardar la foto de perfil: un data-URL base64 (data:image/...;base64,...).
@Data
public class AvatarRequestDTO {
    @NotBlank
    private String avatar;
}
