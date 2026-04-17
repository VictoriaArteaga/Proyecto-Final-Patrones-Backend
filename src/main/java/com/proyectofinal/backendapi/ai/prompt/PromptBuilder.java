package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.Project;
import java.util.Map;

public interface PromptBuilder {

    String buildInitialPrompt(Project project);
    String buildParameterizedPrompt(Project project, Map<String, Object> parameters);
}
