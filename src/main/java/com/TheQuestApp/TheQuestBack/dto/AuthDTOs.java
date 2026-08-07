package com.TheQuestApp.TheQuestBack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class AuthDTOs {


    public record RegisterRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 3, message = "Nome deve ter no mínimo 3 caracteres")
            String name,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
            String password
    ) {}


    public record LoginRequest(
            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            String password
    ) {}


    public record AuthResponse(
            String token,
            UserDTO user
    ) {}


    public record UserDTO(
            Long id,
            String name,
            String email,
            String avatarUrl
    ) {}
}