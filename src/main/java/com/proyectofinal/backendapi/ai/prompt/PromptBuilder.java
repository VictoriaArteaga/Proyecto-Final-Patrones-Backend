package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.ProjectParameters;


public interface PromptBuilder {

    // Prompt para la primera generación, solo a partir de la imagen del terreno.
    String buildInitialPrompt(Project project, String userDescription);

    // Prompt avanzado con los parámetros del proyecto (tras un rechazo).
    String buildParameterizedPrompt(Project project, ProjectParameters parameters, String userDescription);
}
