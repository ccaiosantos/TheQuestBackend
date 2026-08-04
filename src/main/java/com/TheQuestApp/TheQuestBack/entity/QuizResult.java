package com.TheQuestApp.TheQuestBack.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "wrong_answers", nullable = false)
    private Integer wrongAnswers;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "accuracy_percent")
    private Double accuracyPercent;    // 0-100

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
        if (totalQuestions != null && totalQuestions > 0) {
            accuracyPercent = (correctAnswers * 100.0) / totalQuestions;
        }
    }
}
