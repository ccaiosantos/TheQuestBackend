package com.TheQuestApp.TheQuestBack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alternatives")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alternative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 1)
    private String letter;            // A, B, C, D, E

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;              // Texto da alternativa

    @Column(name = "is_correct", nullable = false)
    private boolean correct;



}
