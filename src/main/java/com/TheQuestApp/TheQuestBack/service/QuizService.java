package com.TheQuestApp.TheQuestBack.service;

import com.TheQuestApp.TheQuestBack.dto.QuizDTOs;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.GenerateQuizRequest;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.QuizResultResponse;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.SubmitQuizRequest;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.AlternativeResponse;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.QuestionResponse;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.RecentQuizDTO;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.UserStatsResponse;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.QuizResponse;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.AnswerDTO;
import com.TheQuestApp.TheQuestBack.entity.*;
import com.TheQuestApp.TheQuestBack.repository.DocumentRepository;
import com.TheQuestApp.TheQuestBack.repository.QuizRepository;
import com.TheQuestApp.TheQuestBack.repository.QuizResultRepository;
import com.TheQuestApp.TheQuestBack.repository.VideoContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final DocumentRepository documentRepository;
    private final VideoContentRepository videoContentRepository;
    private final GeminiService geminiService;

    @Transactional
    public QuizResponse generateQuiz(QuizDTOs.GenerateQuizRequest request, User user) {
        // 1. Obtém o conteúdo base (PDF ou vídeo)
        String content = extractContent(request, user.getId());

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "O documento não possui texto extraído. Verifique se o PDF não é uma imagem escaneada."
            );
        }

        log.info("Gerando {} questões tipo {} para usuário {}", request.quantity(), request.examType(), user.getId());
        List<GeminiService.GeneratedQuestion> generated = geminiService.generateQuestions(content, request);

        Document document = request.documentId() != null
                ? documentRepository.findById(request.documentId()).orElse(null)
                : null;

        VideoContent video = request.videoId() != null
                ? videoContentRepository.findById(request.videoId()).orElse(null)
                : null;

        var quiz = Quiz.builder()
                .user(user)
                .document(document)
                .videoContent(video)
                .examType(request.examType())
                .banca(request.banca())
                .difficulty(request.difficulty())
                .format(request.format())
                .questionCount(generated.size())
                .questions(new ArrayList<>())
                .build();

        int order = 1;
        for (var gen : generated) {
            var question = Question.builder()
                    .quiz(quiz)
                    .statement(gen.statement)
                    .subject(gen.subject)
                    .examType(request.examType())
                    .banca(request.banca())
                    .explanation(gen.explanation)
                    .questionOrder(order++)
                    .alternatives(new ArrayList<>())
                    .build();

            for (var genAlt : gen.alternatives) {
                var alt = Alternative.builder()
                        .question(question)
                        .letter(genAlt.letter)
                        .text(genAlt.text)
                        .correct(genAlt.correct)
                        .build();
                question.getAlternatives().add(alt);
            }

            quiz.getQuestions().add(question);
        }

        var saved = quizRepository.save(quiz);
        log.info("Quiz {} criado com {} questões", saved.getId(), saved.getQuestions().size());

        return toQuizResponse(saved);
    }

    public QuizResponse getQuiz(Long quizId, Long userId) {
        var quiz = quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz não encontrado."));
        return toQuizResponse(quiz);
    }

    @Transactional
    public QuizResultResponse submitQuiz(Long quizId, SubmitQuizRequest request, Long userId) {
        var quiz = quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz não encontrado."));

        if (quiz.getResult() != null) {
            throw new IllegalStateException("Este quiz já foi submetido.");
        }

        Map<Long, Long> userAnswers = request.answers().stream()
                .collect(Collectors.toMap(QuizDTOs.AnswerDTO::questionId, AnswerDTO::alternativeId));

        int correct = 0;
        int wrong = 0;

        for (Question question : quiz.getQuestions()) {
            Long chosenAltId = userAnswers.get(question.getId());
            if (chosenAltId == null) {
                wrong++;
                continue;
            }

            boolean isCorrect = question.getAlternatives().stream()
                    .anyMatch(alt -> Objects.equals(alt.getId(), chosenAltId) && alt.isCorrect());

            if (isCorrect) correct++;
            else wrong++;
        }

        var result = QuizResult.builder()
                .quiz(quiz)
                .correctAnswers(correct)
                .wrongAnswers(wrong)
                .totalQuestions(quiz.getQuestions().size())
                .durationSeconds(request.durationSeconds() != null ? request.durationSeconds() : 0)
                .build();

        var saved = quizResultRepository.save(result);

        return new QuizResultResponse(
                saved.getId(),
                quizId,
                saved.getCorrectAnswers(),
                saved.getWrongAnswers(),
                saved.getTotalQuestions(),
                saved.getAccuracyPercent(),
                saved.getDurationSeconds()
        );
    }

    public UserStatsResponse getUserStats(Long userId) {
        long totalQ = quizRepository.countTotalQuestionsByUserId(userId);
        double avgAccuracy = quizRepository.avgAccuracyByUserId(userId);
        long totalSecs = quizResultRepository.sumDurationSecondsByUserId(userId);

        List<Quiz> recent = quizRepository.findRecentCompletedByUserId(userId);
        List<RecentQuizDTO> recentDTOs = recent.stream()
                .limit(5)
                .map(q -> {
                    String title = q.getDocument() != null
                            ? q.getDocument().getFileName()
                            : (q.getVideoContent() != null ? q.getVideoContent().getTitle() : "Quiz");
                    int duration = q.getResult() != null
                            ? q.getResult().getDurationSeconds() / 60
                            : 0;
                    int correctAns = q.getResult() != null ? q.getResult().getCorrectAnswers() : 0;
                    return new RecentQuizDTO(
                            q.getId(),
                            title.replace(".pdf", ""),
                            q.getQuestionCount(),
                            correctAns,
                            duration,
                            q.getExamType()
                    );
                })
                .toList();

        return new UserStatsResponse(
                totalQ,
                (int) Math.round(avgAccuracy),
                (int) (totalSecs / 3600),
                0,
                recentDTOs.size() * 10,
                100,
                recentDTOs
        );
    }

    private String extractContent(GenerateQuizRequest request, Long userId) {
        if (request.documentId() != null) {
            return documentRepository.findByIdAndUserId(request.documentId(), userId)
                    .map(Document::getExtractedText)
                    .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado."));
        }
        if (request.videoId() != null) {
            return videoContentRepository.findByIdAndUserId(request.videoId(), userId)
                    .map(VideoContent::getTranscript)
                    .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado."));
        }
        throw new IllegalArgumentException("É necessário informar documentId ou videoId.");
    }

    private QuizResponse toQuizResponse(Quiz quiz) {
        var questions = quiz.getQuestions().stream()
                .map(q -> new QuestionResponse(
                        q.getId(),
                        q.getStatement(),
                        q.getSubject(),
                        q.getExamType(),
                        q.getBanca(),
                        q.getExplanation(),
                        q.getQuestionOrder(),
                        q.getAlternatives().stream()
                                .map(a -> new AlternativeResponse(a.getId(), a.getLetter(), a.getText(), a.isCorrect()))
                                .toList()
                ))
                .toList();

        return new QuizResponse(
                quiz.getId(),
                quiz.getExamType(),
                quiz.getBanca(),
                quiz.getDifficulty(),
                quiz.getFormat(),
                questions
        );
    }
}

