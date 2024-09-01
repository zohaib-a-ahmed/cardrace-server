package com.cardrace.cardrace_server.controller;

import com.cardrace.cardrace_server.dto.AuthResponse;
import com.cardrace.cardrace_server.dto.LoginRequest;
import com.cardrace.cardrace_server.dto.SignupRequest;
import com.cardrace.cardrace_server.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostConstruct
    public void init() {
        logger.info("AuthController initialized with mappings:");
        logger.info("/api/auth/signup (POST)");
        logger.info("/api/auth/login (POST)");
    }

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.registerUser(signupRequest);
        logger.info("User registered successfully: {}", signupRequest.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        logger.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok(response);
    }
}