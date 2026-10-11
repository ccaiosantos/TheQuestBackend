package com.TheQuestApp.TheQuestBack.controller;

import com.TheQuestApp.TheQuestBack.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.RegisterRequest;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.LoginRequest;

/**
 * AuthController — endpoints públicos de autenticação.
 *
 * POST /api/auth/register → cria conta + retorna JWT
 * POST /api/auth/login    → autentica + retorna JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Criar nova conta")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
