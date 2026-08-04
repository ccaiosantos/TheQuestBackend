package com.TheQuestApp.TheQuestBack.repository;

import com.TheQuestApp.TheQuestBack.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;

import java.util.Optional;

public interface QuizResultRepository extends JpaRepository<QuizResult,Long> {

    Optional<QuizResult> findByQuizId(Long quizId);

    // Soma total de horas de estudo (baseado em durationSeconds)
    @Query("""
        SELECT COALESCE(SUM(r.durationSeconds), 0)
        FROM QuizResult r WHERE r.quiz.user.id = :userId
        """)
    Long sumDurationSecondsByUserId(Long userId);

}
