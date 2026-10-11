package com.TheQuestApp.TheQuestBack.controller;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.QuizResponse;
import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.GenerateQuizRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.QuizResultResponse;
import jakarta.validation.Valid;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.SubmitQuizRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Geração e resolução de questões com IA")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    @Operation(summary = "Gerar questões com IA a partir de PDF ou vídeo")
    public ResponseEntity<QuizResponse> generate(
            @Valid @RequestBody GenerateQuizRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(quizService.generateQuiz(request, user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar quiz por ID")
    public ResponseEntity<QuizResponse> getQuiz(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(quizService.getQuiz(id, user.getId()));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submeter respostas do quiz")
    public ResponseEntity<QuizResultResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitQuizRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(quizService.submitQuiz(id, request, user.getId()));
    }
}
