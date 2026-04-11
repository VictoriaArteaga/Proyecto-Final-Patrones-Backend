package com.proyectofinal.backendapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    private String password; // se guardará encriptada (BCrypt)

    @Email
    @Column(unique = true, nullable = false)
    private String email;

//    correo alternativo para recuperación
    private String recoveryEmail;

//    token temporal para reset de contraseña
    private String passwordResetToken;

//    cuándo vence ese token (seguridad)
    private LocalDateTime passwordResetTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column(columnDefinition = "boolean default false")
    private boolean twoFactorEnabled = false;

    private String twoFactorCode;
    private LocalDateTime twoFactorCodeExpiry;

}
