package com.TheQuestApp.TheQuestBack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;         // Pode ser nulo se veio de vídeo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_content_id")
    private VideoContent videoContent; // Pode ser nulo se veio de PDF

    @Column(name = "exam_type", nullable = false)
    private String examType;

    @Column
    private String banca;

    @Column
    private String difficulty;

    @Column
    private String format;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("questionOrder ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToOne(mappedBy = "quiz", cascade = CascadeType.ALL)
    private QuizResult result;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
