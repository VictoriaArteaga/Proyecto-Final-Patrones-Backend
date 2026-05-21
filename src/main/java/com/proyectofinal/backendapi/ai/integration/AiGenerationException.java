package com.proyectofinal.backendapi.ai.integration;

// Excepción específica para fallos durante la generación de imágenes con IA.
public class AiGenerationException extends RuntimeException {

    public AiGenerationException(String message) {
        super(message);
    }

    public AiGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
