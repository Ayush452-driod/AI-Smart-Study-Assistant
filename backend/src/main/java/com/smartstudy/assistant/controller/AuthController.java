package com.smartstudy.assistant.controller;

import com.smartstudy.assistant.dto.AuthRequest;
import com.smartstudy.assistant.dto.LoginRequest;
import com.smartstudy.assistant.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody AuthRequest request) {

        try {
            return ResponseEntity.ok(
                    authService.signup(request)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            return ResponseEntity.ok(
                    authService.login(request)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(401)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
