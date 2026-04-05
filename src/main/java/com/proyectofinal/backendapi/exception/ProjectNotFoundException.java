package com.proyectofinal.backendapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProjectNotFoundException extends RuntimeException{

    public ProjectNotFoundException() {
        super("Project not found or you do not have access permissions.");
    }
}
