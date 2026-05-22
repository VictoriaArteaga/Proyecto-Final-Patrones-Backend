package com.proyectofinal.backendapi.ai.dto;

// Modo de generación del render 2D.
// INITIAL: primera generación a partir de la imagen del terreno.
// WITH_PARAMETERS: re-generación tras un rechazo, usando los parámetros del proyecto.
public enum RenderMode {
    INITIAL,
    WITH_PARAMETERS
}
