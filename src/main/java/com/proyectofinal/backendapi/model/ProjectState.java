package com.proyectofinal.backendapi.model;

public enum ProjectState {

    IMAGE_UPLOADED, // Imagen cargada.
    GENERATING_2D, // Generando Imagen 2d.
    WAITING_2D_APPROVAL, // Esperando aprobación 2D.
    REJECTED_2D, // 2D Rechazado.
    GENERATING_2D_WITH_PARAMS, // Generando Imagen 2D con parámetros.
    WAITING_FINAL_APPROVAL, // Esperando Aprobación final.
    GENERATING_3D_MODEL, // Generando modelo 3D.
    COMPLETED, // Completado.
    FAILED // Error en proceso.
}
