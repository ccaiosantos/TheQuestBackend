package com.TheQuestApp.TheQuestBack.service;

import com.TheQuestApp.TheQuestBack.dto.QuizDTOs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.TheQuestApp.TheQuestBack.dto.QuizDTOs.GenerateQuizRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.max-tokens}")
    private int maxTokens;

    @Value("${gemini.temperature}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<GeneratedQuestion> generateQuestions(String content, QuizDTOs.GenerateQuizRequest request) {
        String prompt = buildPrompt(content, request);
        String rawResponse = callGeminiApi(prompt);
        return parseResponse(rawResponse);
    }

    // ===== PROMPT BUILDER =====

    private String buildPrompt(String content, GenerateQuizRequest request) {
        String difficultyDesc = switch (request.difficulty()) {
            case "EASY" -> "fácil, conceitos básicos e diretos";
            case "HARD" -> "difícil, raciocínio avançado, detalhes específicos e casos complexos";
            default ->   "médio, compreensão moderada do conteúdo";
        };

        String examStyle = buildExamStyleInstructions(request);

        return """
                Você é um especialista em criação de questões para concursos e exames brasileiros.
                
                Crie EXATAMENTE %d questões de múltipla escolha baseadas EXCLUSIVAMENTE no conteúdo abaixo.
                
                CONFIGURAÇÕES:
                - Tipo de prova: %s
                - Nível de dificuldade: %s
                - Quantidade: %d questões
                %s
                
                CONTEÚDO BASE (use apenas este conteúdo):
                ---
                %s
                ---
                
                REGRAS OBRIGATÓRIAS:
                1. Cada questão deve ter EXATAMENTE 4 alternativas (A, B, C, D)
                2. Apenas UMA alternativa deve ser correta
                3. As alternativas incorretas devem ser plausíveis mas claramente erradas para quem conhece o assunto
                4. A explicação deve citar o trecho do conteúdo que fundamenta a resposta correta
                5. Identifique o assunto/disciplina principal de cada questão
                6. NÃO invente informações que não estejam no conteúdo fornecido
                
                RESPONDA APENAS COM JSON VÁLIDO, sem markdown, sem texto adicional:
                {
                  "questions": [
                    {
                      "statement": "Enunciado completo da questão",
                      "subject": "Disciplina/Assunto",
                      "explanation": "Explicação detalhada da resposta correta",
                      "alternatives": [
                        { "letter": "A", "text": "Texto da alternativa A", "correct": false },
                        { "letter": "B", "text": "Texto da alternativa B", "correct": true },
                        { "letter": "C", "text": "Texto da alternativa C", "correct": false },
                        { "letter": "D", "text": "Texto da alternativa D", "correct": false }
                      ]
                    }
                  ]
                }
                """.formatted(
                request.quantity(),
                request.examType(),
                difficultyDesc,
                request.quantity(),
                examStyle,
                truncateContent(content, 8000) // Limita para não exceder o context window
        );
    }

    private String buildExamStyleInstructions(GenerateQuizRequest request) {
        return switch (request.examType()) {
            case "CONCURSO" -> """
                    - Estilo de banca: %s
                    - Use linguagem formal e técnica
                    - Questões precisas, sem ambiguidade
                    - Inclua casos práticos quando relevante
                    %s
                    """.formatted(
                    request.banca() != null ? request.banca() : "Geral",
                    getBancaInstructions(request.banca())
            );
            case "ENEM" -> """
                    - Contextualize as questões com situações do cotidiano
                    - Use textos motivadores quando possível
                    - Foque em competências e habilidades
                    - Linguagem acessível mas rigorosa
                    """;
            case "OAB" -> """
                    - Foque em aplicação prática do direito
                    - Inclua casos concretos e conflitos jurídicos
                    - Cite artigos e dispositivos legais quando relevante
                    - Questões de raciocínio jurídico
                    """;
            case "VESTIBULAR" -> """
                    - Questões contextualizadas e interdisciplinares
                    - Estimule o raciocínio lógico
                    - Linguagem clara e objetiva
                    """;
            case "RESIDENCIA" -> """
                    - Foque em diagnóstico, conduta e tratamento
                    - Use casos clínicos como base
                    - Terminologia médica adequada
                    - Baseie-se em protocolos e evidências atuais
                    """;
            default -> "- Siga o estilo acadêmico padrão brasileiro\n";
        };
    }

    private String getBancaInstructions(String banca) {
        if (banca == null) return "";
        return switch (banca) {
            case "Cebraspe" -> """
                    - Estilo Cebraspe/Cespe: enunciados com afirmação direta que o candidato julga Certo ou Errado
                    - Alternativas curtas e assertivas, sem rodeios
                    - Explore detalhes, exceções e pegadinhas da lei ou do conteúdo
                    - Frases negativas e duplas negações são comuns — use com moderação
                    """;
            case "FGV" -> """
                    - Estilo FGV: questões elaboradas com textos introdutórios e casos práticos complexos
                    - Enunciados longos que exigem interpretação e raciocínio aplicado
                    - Alternativas com redação cuidadosa; erros sutis nas incorretas
                    - Linguagem formal e vocabulário técnico elevado
                    """;
            case "AOCP" -> """
                    - Estilo AOCP: questões objetivas e diretas, foco no conteúdo programático literal
                    - Enunciados claros e sem ambiguidade
                    - Alternativas bem distribuídas em extensão; evite alternativas muito díspares
                    - Comum em concursos de nível médio e superior de órgãos públicos regionais
                    """;
            case "IBFC" -> """
                    - Estilo IBFC: questões de nível médio a superior, linguagem acessível
                    - Enunciados diretos com situações práticas do dia a dia profissional
                    - Alternativas com extensão similar entre si
                    - Frequente em concursos de saúde, educação e administrativo
                    """;
            case "IDECAN" -> """
                    - Estilo IDECAN: questões objetivas com foco em legislação, normas e regulamentos
                    - Enunciados baseados no conteúdo programático de forma literal
                    - Alternativas curtas e precisas; apenas uma claramente correta
                    - Comum em concursos municipais e estaduais
                    """;
            default -> "";
        };
    }
    private String callGeminiApi(String prompt) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", maxTokens,
                        "responseMimeType", "application/json"
                )
        );

        var entity = new HttpEntity<>(body, headers);
        String url = apiUrl + "?key=" + apiKey;

        log.info("Chamando Gemini API para geração de questões...");

        var response = restTemplate.postForEntity(url, entity, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Erro na API Gemini: " + response.getStatusCode());
        }


        try {
            return response.getBody()
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair resposta do Gemini: " + e.getMessage());
        }
    }

    private List<GeneratedQuestion> parseResponse(String jsonResponse) {
        try {
            // Remove possíveis backticks de markdown que o modelo pode retornar
            String clean = jsonResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode root = objectMapper.readTree(clean);
            JsonNode questionsNode = root.path("questions");

            List<GeneratedQuestion> result = new ArrayList<>();

            for (JsonNode qNode : questionsNode) {
                var question = new GeneratedQuestion();
                question.statement = qNode.path("statement").asText();
                question.subject = qNode.path("subject").asText();
                question.explanation = qNode.path("explanation").asText();
                question.alternatives = new ArrayList<>();

                for (JsonNode altNode : qNode.path("alternatives")) {
                    var alt = new GeneratedAlternative();
                    alt.letter = altNode.path("letter").asText();
                    alt.text = altNode.path("text").asText();
                    alt.correct = altNode.path("correct").asBoolean();
                    question.alternatives.add(alt);
                }

                result.add(question);
            }

            log.info("Gemini gerou {} questões com sucesso.", result.size());
            return result;

        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Gemini: {}", jsonResponse);
            throw new RuntimeException("Erro ao processar resposta da IA: " + e.getMessage());
        }
    }

    private String truncateContent(String content, int maxChars) {
        if (content == null) return "";
        return content.length() > maxChars
                ? content.substring(0, maxChars) + "\n[... conteúdo truncado ...]"
                : content;
    }



    public static class GeneratedQuestion {
        public String statement;
        public String subject;
        public String explanation;
        public List<GeneratedAlternative> alternatives;
    }

    public static class GeneratedAlternative {
        public String letter;
        public String text;
        public boolean correct;
    }
}
