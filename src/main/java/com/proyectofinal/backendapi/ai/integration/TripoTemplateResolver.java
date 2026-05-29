package com.proyectofinal.backendapi.ai.integration;

import com.proyectofinal.backendapi.model.Project;
import com.proyectofinal.backendapi.model.ProjectParameters;
import org.springframework.stereotype.Component;

import java.util.Map;

// Centraliza el mapeo de DesignCategory + ProjectParameters a las
// banderas/templates específicos que entiende la API de Tripo3D.
@Component
public class TripoTemplateResolver {

    // Muta el mapa de parámetros avanzados de Tripo añadiendo los flags adecuados.
    public void apply(Map<String, Object> tripoParams,
                      Project project,
                      ProjectParameters parameters) {

        switch (project.getCategory()) {
            case EXTERIOR_ARCHITECTURE -> applyExterior(tripoParams, parameters);
            case FURNITURE_ITEM -> tripoParams.put("template", "asset_extraction");
        }
    }

    private void applyExterior(Map<String, Object> tripoParams, ProjectParameters parameters) {
        if (parameters == null || parameters.getConstructionType() == null) {
            return;
        }
        String type = parameters.getConstructionType().toLowerCase();
        if (type.contains("boceto") || type.contains("sketch")) {
            tripoParams.put("sketch_to_render", true);
        } else {
            tripoParams.put("template", "3d_enhance");
        }
    }
}
