package com.proyectofinal.backendapi.ai.validation;

import com.proyectofinal.backendapi.exception.BadRequestException;
import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

@Component
public class FurnitureParametersValidator implements ParametersValidator {

    @Override
    public void validate(ProjectParameters parameters) {
        if (parameters == null
                || parameters.getFurnitureType() == null
                || parameters.getFurnitureType().isBlank()) {
            throw new BadRequestException(
                    "Para un proyecto FURNITURE_ITEM el campo 'furnitureType' es obligatorio "
                            + "(ej. estanteria, sofa, mesa, lampara).");
        }
        requirePositiveOrNull(parameters.getFurnitureWidthCm(), "furnitureWidthCm");
        requirePositiveOrNull(parameters.getFurnitureHeightCm(), "furnitureHeightCm");
        requirePositiveOrNull(parameters.getFurnitureDepthCm(), "furnitureDepthCm");
    }

    @Override
    public DesignCategory supportedCategory() {
        return DesignCategory.FURNITURE_ITEM;
    }

    private void requirePositiveOrNull(Double value, String field) {
        if (value != null && value <= 0) {
            throw new BadRequestException(field + " debe ser mayor a 0.");
        }
    }
}
