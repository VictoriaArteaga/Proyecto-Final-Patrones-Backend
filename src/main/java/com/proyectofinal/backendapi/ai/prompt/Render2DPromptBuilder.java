package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;

@Component
public class Render2DPromptBuilder implements PromptBuilder {

    private static final String BASE_STYLE =
            "photorealistic architectural concept render, modern architecture, professional lighting, "
                    + "high detail, ultra realistic, 8k, daylight, soft shadows";

    @Override
    public String buildInitialPrompt(Project project, String userDescription) {
        return new PromptInternalBuilder()
                .base("Architectural 2D concept render based on a real photograph.")
                .projectContext(project.getName())
                .userDescription(userDescription)
                .style(BASE_STYLE)
                .build();
    }

    @Override
    public String buildParameterizedPrompt(Project project,
                                           ProjectParameters parameters,
                                           String userDescription) {

        PromptInternalBuilder builder = new PromptInternalBuilder()
                .base("Architectural 2D concept render of a structure placed on a real photograph.")
                .projectContext(project.getName());

        if (parameters != null) {
            builder
                    .dimensions(parameters.getLotWidth(), parameters.getLotLength(), parameters.getTotalArea())
                    .constructionType(parameters.getConstructionType())
                    .color(parameters.getColor())
                    .floors(parameters.getNumberOfFloors())
                    .rooms(parameters.getNumberOfRooms(), parameters.getNumberOfBathrooms())
                    .additionalElements(parameters.getAdditionalElements())
                    .detailDescription(parameters.getDetailDescription());
        }

        return builder
                .userDescription(userDescription)
                .style(BASE_STYLE)
                .build();
    }

    // Builder interno: acumula fragmentos, limpia Unicode y recorta a 1024 caracteres.
    private static class PromptInternalBuilder {

        private final StringBuilder prompt = new StringBuilder();

        PromptInternalBuilder base(String value) {
            return append(value);
        }

        PromptInternalBuilder projectContext(String projectName) {
            if (isNotBlank(projectName)) {
                append("Project name: " + projectName + ".");
            }
            return this;
        }

        PromptInternalBuilder userDescription(String description) {
            if (isNotBlank(description)) {
                append("User description: " + description.trim() + ".");
            }
            return this;
        }

        PromptInternalBuilder dimensions(Double width, Double length, Double area) {
            if (width != null && length != null) {
                append(String.format("Lot dimensions: %.2f m x %.2f m.", width, length));
            }
            if (area != null) {
                append(String.format("Total area: %.2f square meters.", area));
            }
            return this;
        }

        PromptInternalBuilder constructionType(String type) {
            if (isNotBlank(type)) {
                append("Construction type: " + type + ".");
            }
            return this;
        }

        PromptInternalBuilder color(String color) {
            if (isNotBlank(color)) {
                append("Predominant color: " + color + ".");
            }
            return this;
        }

        PromptInternalBuilder floors(Integer floors) {
            if (floors != null && floors > 0) {
                append("Number of floors: " + floors + ".");
            }
            return this;
        }

        PromptInternalBuilder rooms(Integer rooms, Integer bathrooms) {
            if (rooms != null && rooms > 0) {
                append("Number of rooms: " + rooms + ".");
            }
            if (bathrooms != null && bathrooms > 0) {
                append("Number of bathrooms: " + bathrooms + ".");
            }
            return this;
        }

        PromptInternalBuilder additionalElements(List<String> elements) {
            if (elements != null && !elements.isEmpty()) {
                append("Additional elements: " + String.join(", ", elements) + ".");
            }
            return this;
        }

        PromptInternalBuilder detailDescription(String detail) {
            if (isNotBlank(detail)) {
                append("Extra details: " + detail.trim() + ".");
            }
            return this;
        }

        PromptInternalBuilder style(String style) {
            return append("Style: " + style + ".");
        }

        String build() {
            String rawPrompt = prompt.toString().trim();

            // 1. Remover acentos y diacríticos.
            String normalized = Normalizer.normalize(rawPrompt, Normalizer.Form.NFD);
            normalized = normalized.replaceAll("\\p{M}", "");

            // 2. Filtrar emojis y caracteres Unicode especiales.
            String sanitized = normalized.replaceAll("[^\\p{L}\\p{N}\\s.,;:\\-_()'\"]", "").trim();

            // 3. Controlar la restricción estricta de 1024 caracteres de Tripo.
            if (sanitized.length() > 1024) {
                return sanitized.substring(0, 1024);
            }

            return sanitized;
        }

        private PromptInternalBuilder append(String text) {
            if (prompt.length() > 0) {
                prompt.append(' ');
            }
            prompt.append(text);
            return this;
        }

        private static boolean isNotBlank(String value) {
            return value != null && !value.isBlank();
        }
    }
}