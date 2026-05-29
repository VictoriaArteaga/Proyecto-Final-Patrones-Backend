package com.proyectofinal.backendapi.ai.validation;

import com.proyectofinal.backendapi.exception.BadRequestException;
import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

@Component
public class ExteriorParametersValidator implements ParametersValidator {

    @Override
    public void validate(ProjectParameters parameters) {
        if (parameters == null) {
            return;
        }
        requirePositiveOrNull(parameters.getLotWidth(), "lotWidth");
        requirePositiveOrNull(parameters.getLotLength(), "lotLength");
        requirePositiveOrNull(parameters.getTotalArea(), "totalArea");
        requireNonNegativeOrNull(parameters.getNumberOfFloors(), "numberOfFloors");
        requireNonNegativeOrNull(parameters.getNumberOfRooms(), "numberOfRooms");
        requireNonNegativeOrNull(parameters.getNumberOfBathrooms(), "numberOfBathrooms");
    }

    @Override
    public DesignCategory supportedCategory() {
        return DesignCategory.EXTERIOR_ARCHITECTURE;
    }

    private void requirePositiveOrNull(Double value, String field) {
        if (value != null && value <= 0) {
            throw new BadRequestException(field + " debe ser mayor a 0.");
        }
    }

    private void requireNonNegativeOrNull(Integer value, String field) {
        if (value != null && value < 0) {
            throw new BadRequestException(field + " no puede ser negativo.");
        }
    }
}
