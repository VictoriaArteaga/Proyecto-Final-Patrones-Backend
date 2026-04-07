package com.proyectofinal.backendapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cors")
public class AppCorsProperties {
    private List<String> allowedOrigins;
}
