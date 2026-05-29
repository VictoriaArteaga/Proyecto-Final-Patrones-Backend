package com.proyectofinal.backendapi.ai.prompt;

import com.proyectofinal.backendapi.model.DesignCategory;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;


@Component
public class InteriorRoomPromptBuilder extends BasePromptBuilder {

    private static final String EDIT_INSTRUCTION_INITIAL =
            "Edit the reference photograph of the interior space. Preserve the walls, floor, ceiling, "
                    + "windows, doors and existing lighting exactly as shown. Restyle the room by "
                    + "modifying the furniture, decor, color palette and finishes to match the user "
                    + "requirements below.";

    private static final String EDIT_INSTRUCTION_PARAMETERIZED =
            "Edit the reference photograph of the interior space. Preserve the walls, floor, ceiling, "
                    + "windows, doors and existing lighting exactly as shown. Restyle the room by "
                    + "modifying the furniture, decor, color palette and finishes to match the user "
                    + "requirements and parameters below.";

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
                .roomType(params.getRoomType())
                .styleTrend(params.getStyleTrend())
                .color(params.getColor())
                .materials(params.getMaterials())
                .additionalElements(params.getAdditionalElements())
                .detailDescription(params.getDetailDescription());
    }

    @Override
    public DesignCategory supportedCategory() {
        return DesignCategory.INTERIOR_ROOM;
    }

    @Override
    protected String fidelityLockText() {
        return "Strict fidelity: modify ONLY the furniture, decor, color palette and finishes as "
                + "described by the user. The architecture of the room (walls, floor, ceiling, "
                + "windows, doors), the perspective and the camera framing must remain identical to "
                + "the reference photograph. Do not invent or remove structural elements.";
    }
}
