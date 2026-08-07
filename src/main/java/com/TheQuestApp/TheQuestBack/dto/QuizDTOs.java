package com.TheQuestApp.TheQuestBack.dto;

import jakarta.validation.constraints.*;

import java.util.List;


public class QuizDTOs {

    public record GenerateQuizRequest(
            Long documentId,           // Pode ser nulo se veio de vídeo
            Long videoId,              // Pode ser nulo se veio de PDF

            @NotBlank
            String examType,           // CONCURSO, ENEM, VESTIBULAR, etc.

            String banca,              // Apenas para CONCURSO

            @NotNull
            @Min(1) @Max(50)
            Integer quantity,

            @NotBlank
            String difficulty,         // EASY, MEDIUM, HARD

            @NotBlank
            String format              // OBJECTIVE, ESSAY
    ) {}

    /** Resposta do quiz com todas as questões */
    public record QuizResponse(
            Long id,
            String examType,
            String banca,
            String difficulty,
            String format,
            List<QuestionResponse> questions
    ) {}

    public record QuestionResponse(
            Long id,
            String statement,
            String subject,
            String examType,
            String banca,
            String explanation,
            Integer questionOrder,
            List<AlternativeResponse> alternatives
    ) {}

    public record AlternativeResponse(
            Long id,
            String letter,
            String text,
            boolean correct
    ) {}


    public record SubmitQuizRequest(
            @NotEmpty
            List<AnswerDTO> answers,

            Integer durationSeconds
    ) {}

    public record AnswerDTO(
            @NotNull Long questionId,
            @NotNull Long alternativeId
    ) {}


    public record QuizResultResponse(
            Long id,
            Long quizId,
            int correctAnswers,
            int wrongAnswers,
            int totalQuestions,
            double accuracyPercent,
            int durationSeconds
    ) {}


    public record UserStatsResponse(
            long totalQuestions,
            int accuracy,
            int studyHours,
            int streak,
            int weeklyProgress,
            int weeklyGoal,
            List<RecentQuizDTO> recentQuizzes
    ) {}

    public record RecentQuizDTO(
            Long id,
            String title,
            int totalQuestions,
            int correctAnswers,
            int durationMinutes,
            String examType
    ) {}
}