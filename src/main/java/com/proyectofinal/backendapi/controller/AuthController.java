package com.proyectofinal.backendapi.controller;


import com.proyectofinal.backendapi.model.User;
import com.proyectofinal.backendapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService userService;

    // REGISTRO
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    // LOGIN
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        return userService.login(user.getEmail(), user.getPassword());
    }
}
