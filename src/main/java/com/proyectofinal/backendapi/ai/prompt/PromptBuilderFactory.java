package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.DesignCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Patrón Factory: resuelve qué PromptBuilder usar según la categoría del proyecto.
@Component
public class PromptBuilderFactory {

    private final Map<DesignCategory, PromptBuilder> registry;

    public PromptBuilderFactory(List<PromptBuilder> builders) {
        this.registry = builders.stream()
                .collect(Collectors.toMap(PromptBuilder::supportedCategory, Function.identity()));
    }

    public PromptBuilder forCategory(DesignCategory category) {
        PromptBuilder builder = registry.get(category);
        if (builder == null) {
            throw new IllegalStateException(
                    "No PromptBuilder registrado para la categoría: " + category);
        }
        return builder;
    }
}
