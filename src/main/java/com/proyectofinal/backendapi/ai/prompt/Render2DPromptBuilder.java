package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Render2DPromptBuilder implements PromptBuilder{

    @Override
    public String buildInitialPrompt(Project project) {

        ProjectParameters params = project.getParameters();
        StringBuilder prompt = new StringBuilder();

        // Contexto general.
        appendBaseContext(prompt, project);

        if (params != null) {
            appendProjectParameters(prompt, params);
        }
        else {
            prompt.append("""
                
                Structure: single family residential house
                Style: minimalist architecture
                Context: integrated with the landscape
                """
            );
        }

        // Calidad base del prompt.
        appendBaseQuality(prompt);

        return prompt.toString();
    }

    @Override
    public String buildParameterizedPrompt(Project project,
                                           Map<String, Object> parameters) {

        ProjectParameters params = project.getParameters();
        StringBuilder prompt = new StringBuilder();

        // Contexto general.
        appendBaseContext(prompt, project);

        if (params != null) {
            appendProjectParameters(prompt, params);
        }

        appendDynamicParameters(prompt, parameters);

        appendAdvancedQuality(prompt);

        return prompt.toString();

    }

    // transformación de los datos a texto.
    private void appendProjectParameters(StringBuilder prompt, ProjectParameters params) {

        if (params.getLotWidth() != null && params.getLotLength() != null) {
            prompt.append("Lot size: ")
                    .append(params.getLotWidth()).append("m x ")
                    .append(params.getLotLength()).append("m\n");
        }

        if (params.getTotalArea() != null) {
            prompt.append("Total area: ")
                    .append(params.getTotalArea()).append(" m2\n");
        }

        if (params.getConstructionType() != null ) {
            prompt.append("Construction type: ")
                    .append(params.getConstructionType()).append("\n");
        }

        if (params.getColor() != null) {
            prompt.append("Main color: ")
                    .append(params.getColor()).append("\n");
        }

        if (params.getNumberOfFloors() != null) {
            prompt.append("Floors: ")
                    .append(params.getNumberOfFloors()).append("\n");
        }

        if (params.getNumberOfRooms() != null) {
            prompt.append("Rooms: ")
                    .append(params.getNumberOfRooms()).append("\n");
        }

        if (params.getNumberOfBathrooms() != null) {
            prompt.append("Bathrooms: ")
                    .append(params.getNumberOfBathrooms()).append("\n");
        }

        if (params.getAdditionalElements() != null && !params.getAdditionalElements().isEmpty()) {
            String elements = String.join(", ", params.getAdditionalElements());
            prompt.append("Additional elements: ")
                    .append(elements).append("\n");
        }

        if (params.getDetailDescription() != null) {
            prompt.append("Description: ")
                    .append(params.getDetailDescription()).append("\n");
        }
    }


    // PARÁMETROS DINÁMICOS.
    private void appendDynamicParameters(StringBuilder prompt, Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) return;

        parameters.forEach((key, value) -> {
            if (value != null && !value.toString().isBlank()) {
                prompt.append(capitalize(key))
                        .append(": ")
                        .append(value)
                        .append("\n");
            }
        });
    }

    // CALIDAD BASE
    private void appendBaseQuality(StringBuilder prompt) {
        prompt.append("""
                
                Style: modern architectural visualization
                Lighting: natural light, soft shadows
                Rendering: physically based rendering
                Quality: ultra realistic, 4k, high detail
                """);
    }

    // CALIDAD AVANZADA
    private void appendAdvancedQuality(StringBuilder prompt) {
        prompt.append("""
                
                Style: modern architectural visualization
                Lighting: cinematic lighting, realistic shadows
                Rendering: physically based rendering, global illumination
                Quality: ultra realistic, 4k, high detail, sharp focus
                """);
    }


    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void appendBaseContext(StringBuilder prompt, Project project) {
        prompt.append("High-quality architectural 2D render.\n\n");
        prompt.append("Project: ").append(project.getName()).append("\n");
    }

}
