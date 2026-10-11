package com.TheQuestApp.TheQuestBack.controller;

import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.UserStatsResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Perfil e estatísticas")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final QuizService quizService;

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public ResponseEntity<User> me(@AuthenticationPrincipal User user) {
        // User vem direto do SecurityContext, sem nova query ao banco
        return ResponseEntity.ok(user);
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas do usuário para a Home")
    public ResponseEntity<UserStatsResponse> stats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(quizService.getUserStats(user.getId()));
    }
}
