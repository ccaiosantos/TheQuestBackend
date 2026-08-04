package com.TheQuestApp.TheQuestBack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String statement;          // Enunciado

    @Column
    private String subject;            // Ex: "Direito Constitucional"

    @Column(name = "exam_type")
    private String examType;           // CONCURSO, ENEM, etc.

    @Column
    private String banca;              // Cebraspe, FGV, etc.

    @Column(columnDefinition = "TEXT")
    private String explanation;        // Explicação da IA

    @Column(name = "question_order")
    private Integer questionOrder;     // Posição na lista (1, 2, 3...)

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("letter ASC")
    @Builder.Default
    private List<Alternative> alternatives = new ArrayList<>();
}
