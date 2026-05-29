package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.ProjectParameters;

import java.text.Normalizer;
import java.util.List;

// Patrón Template Method: define el esqueleto común de un prompt 2D
// y delega a las subclases los detalles específicos de cada categoría.
public abstract class BasePromptBuilder implements PromptBuilder {

    protected static final String BASE_STYLE =
            "photorealistic render, professional lighting, ultra realistic, 8k, soft shadows";

    private static final String REFERENCE_LOCK =
            "Match the camera angle, scale and lighting of the reference photo; keep its background "
                    + "intact.";

    // Cláusula por defecto (genérica). Cada subclase puede sobreescribirla para
    // adaptarla al lenguaje de su categoría y evitar contradicciones.
    private static final String DEFAULT_FIDELITY_LOCK =
            "Strict fidelity: the rendered output must include every feature explicitly requested by "
                    + "the user. Do not omit, simplify or substitute any element.";

    // Cláusula para la generación con parámetros: el resultado debe ser 100% fiel
    // a los TRES pilares a la vez (imagen original + parámetros + descripción).
    private static final String PARAM_FIDELITY_LOCK =
            "ABSOLUTE FIDELITY, honor all three at once: (1) use the reference photograph as the base, "
                    + "keeping its exact perspective, scale, lighting and background; (2) apply EVERY "
                    + "parameter listed with its exact value; (3) include EVERY detail of the user "
                    + "requirements. Do not omit, simplify, invent or contradict any of them.";

    @Override
    public final String buildInitialPrompt(Project project, String userDescription) {
        PromptInternalBuilder builder = new PromptInternalBuilder()
                .base(initialEditInstruction())
                .userDescription(userDescription)
                .projectContext(project.getName());
        return builder
                .referenceLock(referenceLockText())
                .fidelityLock(fidelityLockText())
                .style(BASE_STYLE)
                .build();
    }

    @Override
    public final String buildParameterizedPrompt(Project project,
                                                 ProjectParameters parameters,
                                                 String userDescription) {

        // Orden por prioridad para que NADA crítico se pierda si Tripo recorta a
        // 1024 caracteres: 1) descripción del usuario, 2) parámetros exactos,
        // 3) instrucción de edición sobre la foto original, 4) cláusula de fidelidad
        // a los 3 pilares. El estilo va al final (lo único prescindible).
        PromptInternalBuilder builder = new PromptInternalBuilder()
                .userDescription(userDescription)
                .base(parameterizedEditInstruction());

        if (parameters != null) {
            applyParameters(builder, parameters);
        }

        return builder
                .fidelityLock(PARAM_FIDELITY_LOCK)
                .projectContext(project.getName())
                .style(BASE_STYLE)
                .build();
    }

    // --- Hooks del Template Method ---

    // Instrucción de edición para el primer render (sin parámetros).
    protected abstract String initialEditInstruction();

    // Instrucción de edición para el render parametrizado (tras un rechazo).
    protected abstract String parameterizedEditInstruction();

    // Aplica los campos relevantes de ProjectParameters al prompt según la categoría.
    protected abstract void applyParameters(PromptInternalBuilder builder, ProjectParameters parameters);

    // Cláusula de fidelidad: por defecto genérica, sobreescribible por categoría.
    protected String fidelityLockText() {
        return DEFAULT_FIDELITY_LOCK;
    }

    // Cláusula de "anclaje" a la foto de referencia. Por defecto pide conservar
    // cámara, escala, iluminación y fondo (útil para exteriores/objetos). Las
    // categorías que necesitan transformar la escena completa (p. ej. interiores)
    // la sobreescriben para no congelar la imagen.
    protected String referenceLockText() {
        return REFERENCE_LOCK;
    }



    protected static class PromptInternalBuilder {

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
                append("USER REQUIREMENTS (mandatory, the rendered output must include every "
                        + "feature stated here): \"" + description.trim() + "\".");
            }
            return this;
        }

        // --- Métodos para EXTERIOR_ARCHITECTURE ---

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

        // --- Métodos para FURNITURE_ITEM ---

        PromptInternalBuilder furnitureType(String furnitureType) {
            if (isNotBlank(furnitureType)) {
                append("Furniture item: " + furnitureType + ".");
            }
            return this;
        }

        PromptInternalBuilder furnitureDimensions(Double widthCm, Double heightCm, Double depthCm) {
            if (widthCm != null && heightCm != null && depthCm != null) {
                append(String.format("Furniture dimensions: %.0f cm wide x %.0f cm tall x %.0f cm deep.",
                        widthCm, heightCm, depthCm));
            }
            return this;
        }

        PromptInternalBuilder materials(String materials) {
            if (isNotBlank(materials)) {
                append("Materials: " + materials + ".");
            }
            return this;
        }

        PromptInternalBuilder styleTrend(String styleTrend) {
            if (isNotBlank(styleTrend)) {
                append("Style trend: " + styleTrend + ".");
            }
            return this;
        }

        PromptInternalBuilder placement(String placement) {
            if (isNotBlank(placement)) {
                append("Placement in scene: " + placement + ".");
            }
            return this;
        }

        // --- Cierres comunes ---

        PromptInternalBuilder style(String style) {
            return append("Style: " + style + ".");
        }

        PromptInternalBuilder referenceLock(String text) {
            return append(text);
        }

        PromptInternalBuilder fidelityLock(String text) {
            return append(text);
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
