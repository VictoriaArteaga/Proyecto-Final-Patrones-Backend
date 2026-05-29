package com.proyectofinal.backendapi.ai.validation;

import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;

// Estrategia de validación específica para cada categoría de diseño.
// Cada implementación verifica que los parámetros relevantes para su
// categoría estén presentes y sean válidos antes de regenerar el render.
public interface ParametersValidator {

    void validate(ProjectParameters parameters);

    DesignCategory supportedCategory();
}
