package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

@Component
public class FurnitureItemPromptBuilder extends BasePromptBuilder {

    private static final String EDIT_INSTRUCTION_INITIAL =
            "Edit the reference photograph. Keep the room, walls, floor, ceiling, lighting and every "
                    + "existing object exactly as shown. Add a single new furniture item integrated "
                    + "naturally into the scene, matching the user requirements below. Do not remove "
                    + "or alter anything that is already in the photograph.";

    private static final String EDIT_INSTRUCTION_PARAMETERIZED =
            "Edit the reference photograph. Keep the room, walls, floor, ceiling, lighting and every "
                    + "existing object exactly as shown. Add a single new furniture item integrated "
                    + "naturally into the scene, matching the user requirements and parameters below. "
                    + "Do not remove or alter anything that is already in the photograph.";

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
                .furnitureType(params.getFurnitureType())
                .furnitureDimensions(
                        params.getFurnitureWidthCm(),
                        params.getFurnitureHeightCm(),
                        params.getFurnitureDepthCm())
                .materials(params.getMaterials())
                .color(params.getColor())
                .styleTrend(params.getStyleTrend())
                .placement(params.getPlacement())
                .detailDescription(params.getDetailDescription());
    }

    @Override
    public DesignCategory supportedCategory() {
        return DesignCategory.FURNITURE_ITEM;
    }

    @Override
    protected String fidelityLockText() {
        return "Strict fidelity: the ONLY change to the photograph is adding the single new furniture "
                + "item described above. Every other pixel — walls, floor, ceiling, paint, lighting, "
                + "existing objects, perspective and color palette — must remain identical to the "
                + "reference. Do not restyle, repaint or redecorate anything else in the scene.";
    }
}
