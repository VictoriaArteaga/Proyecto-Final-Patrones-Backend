package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

@Component
public class ExteriorArchitecturePromptBuilder extends BasePromptBuilder {

    private static final String EDIT_INSTRUCTION_INITIAL =
            "Edit the reference photograph. Keep its terrain, sky, perspective and surroundings "
                    + "unchanged. Inside the empty buildable area, add a photorealistic 2D render of a "
                    + "building that matches the user requirements below.";

    private static final String EDIT_INSTRUCTION_PARAMETERIZED =
            "Edit the reference photograph. Keep its terrain, sky, perspective and surroundings "
                    + "unchanged. Inside the empty buildable area, add a photorealistic 2D render of a "
                    + "building that matches the user requirements and parameters below.";

    @Override
    protected String initialEditInstruction() {
        return EDIT_INSTRUCTION_INITIAL;
    }

    @Override
    protected String parameterizedEditInstruction() {
        return EDIT_INSTRUCTION_PARAMETERIZED;
    }

    @Override
    protected void applyParameters(PromptInternalBuilder builder, ProjectParameters params) {
        builder
                .dimensions(params.getLotWidth(), params.getLotLength(), params.getTotalArea())
                .constructionType(params.getConstructionType())
                .color(params.getColor())
                .floors(params.getNumberOfFloors())
                .rooms(params.getNumberOfRooms(), params.getNumberOfBathrooms())
                .additionalElements(params.getAdditionalElements())
                .detailDescription(params.getDetailDescription());
    }

    @Override
    public DesignCategory supportedCategory() {
        return DesignCategory.EXTERIOR_ARCHITECTURE;
    }

    @Override
    protected String fidelityLockText() {
        return "Strict fidelity: the rendered building MUST contain every architectural feature "
                + "requested by the user, with the exact number of floors, rooms, garages, balconies, "
                + "windows, materials and any element explicitly mentioned. Do not omit, simplify or "
                + "substitute any element.";
    }
}
