package com.proyectofinal.backendapi.controller;

import org.springframework.web.bind.annotation.*;
        import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "status", "ok",
                "message", "API running"
        );
    }
}
