package com.proyectofinal.backendapi.ai.validation;

import com.proyectofinal.backendapi.model.DesignCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Factory: resuelve el ParametersValidator correcto según la categoría del proyecto.
@Component
public class ParametersValidatorFactory {

    private final Map<DesignCategory, ParametersValidator> registry;

    public ParametersValidatorFactory(List<ParametersValidator> validators) {
        this.registry = validators.stream()
                .collect(Collectors.toMap(ParametersValidator::supportedCategory, Function.identity()));
    }

    public ParametersValidator forCategory(DesignCategory category) {
        ParametersValidator validator = registry.get(category);
        if (validator == null) {
            throw new IllegalStateException(
                    "No ParametersValidator registrado para la categoría: " + category);
        }
        return validator;
    }
}
