package com.proyectofinal.backendapi.ai.validation;

import com.proyectofinal.backendapi.exception.BadRequestException;
import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

@Component
public class InteriorParametersValidator implements ParametersValidator {

    @Override
    public void validate(ProjectParameters parameters) {
        if (parameters == null
                || parameters.getRoomType() == null
                || parameters.getRoomType().isBlank()) {
            throw new BadRequestException(
                    "Para un proyecto INTERIOR_ROOM el campo 'roomType' es obligatorio "
                            + "(ej. sala, cuarto, cocina).");
        }
    }

    @Override
    public DesignCategory supportedCategory() {
        return DesignCategory.INTERIOR_ROOM;
    }
}
