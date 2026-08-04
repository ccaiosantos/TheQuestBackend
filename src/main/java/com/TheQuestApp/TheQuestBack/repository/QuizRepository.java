package com.TheQuestApp.TheQuestBack.repository;

import com.TheQuestApp.TheQuestBack.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Quiz> findByIdAndUserId(Long id, Long userId);

    // Busca os N quizzes mais recentes com resultado já salvo
    @Query("""
        SELECT q FROM Quiz q
        LEFT JOIN FETCH q.result
        WHERE q.user.id = :userId AND q.result IS NOT NULL
        ORDER BY q.createdAt DESC
        """)
    List<Quiz> findRecentCompletedByUserId(Long userId);

    // Conta total de questões respondidas pelo usuário
    @Query("""
        SELECT COALESCE(SUM(r.totalQuestions), 0)
        FROM QuizResult r WHERE r.quiz.user.id = :userId
        """)
    Long countTotalQuestionsByUserId(Long userId);

    // Média de acertos
    @Query("""
        SELECT COALESCE(AVG(r.accuracyPercent), 0)
        FROM QuizResult r WHERE r.quiz.user.id = :userId
        """)
    Double avgAccuracyByUserId(Long userId);
}
