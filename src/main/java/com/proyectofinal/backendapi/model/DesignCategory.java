package com.proyectofinal.backendapi.model;

// Clasifica el tipo de diseño que un proyecto va a generar.
// Selecciona la estrategia de prompt y el mapeo de templates de Tripo.
public enum DesignCategory {

    // Casas, edificios o estructuras sobre un terreno (flujo original).
    EXTERIOR_ARCHITECTURE,

    // Rediseño de un espacio interior completo (sala, cuarto, cocina).
    INTERIOR_ROOM,

    // Inserción de un mueble u objeto puntual en una foto.
    FURNITURE_ITEM
}
