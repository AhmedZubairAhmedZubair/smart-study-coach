package com.ahmed.studycoach.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> generateStudyContent(
            String topic,
            String difficulty,
            int mcqCount,
            String customPrompt
    ) {
        try {
            Map<String, Object> explanationData = generateExplanationAndPractice(
                    topic,
                    difficulty,
                    customPrompt
            );

            List<Map<String, String>> allMcqs = new ArrayList<>();

            int remaining = mcqCount;
            int batchNumber = 1;

            while (remaining > 0) {
                int batchSize = Math.min(5, remaining);

                List<Map<String, String>> batchMcqs = generateMcqBatchWithRetry(
                        topic,
                        difficulty,
                        customPrompt,
                        batchSize,
                        batchNumber
                );

                allMcqs.addAll(batchMcqs);

                remaining -= batchSize;
                batchNumber++;
            }

            return Map.of(
                    "summary", explanationData.get("summary"),
                    "explanation", explanationData.get("explanation"),
                    "mcqs", allMcqs,
                    "practiceProblems", explanationData.get("practiceProblems")
            );

        } catch (Exception e) {
            throw new RuntimeException("Gemini API failed: " + e.getMessage(), e);
        }
    }

    public List<String> generatePriorityBasedPractice(List<Map<String, Object>> priorityTopics) {
        try {
            List<String> focusAreas = new ArrayList<>();

            for (Map<String, Object> topicData : priorityTopics) {
                String topic = topicData.get("topic").toString();
                int count = Integer.parseInt(topicData.get("count").toString());

                for (int i = 0; i < count; i++) {
                    focusAreas.add(topic);
                }
            }

            List<String> allPracticeQuestions = new ArrayList<>();

            int index = 0;
            int batchNumber = 1;

            while (index < focusAreas.size()) {
                int end = Math.min(index + 5, focusAreas.size());
                List<String> batchFocusAreas = focusAreas.subList(index, end);

                List<String> batchQuestions = generatePracticeBatch(batchFocusAreas, batchNumber);

                allPracticeQuestions.addAll(batchQuestions);

                index = end;
                batchNumber++;
            }

            return allPracticeQuestions;

        } catch (Exception e) {
            throw new RuntimeException("Priority practice generation failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> generateExplanationAndPractice(
            String topic,
            String difficulty,
            String customPrompt
    ) throws Exception {

        String prompt = """
                You are an expert AI study coach for software engineering students.

                Topic: %s
                Difficulty: %s
                Extra user instruction: %s

                Return ONLY valid JSON.
                Your first character must be { and your last character must be }.
                Do not write anything before or after JSON.
                Do not use markdown.
                Do not use ```json.

                Use this exact JSON structure:

                {
                  "summary": "simple but useful summary here",
                  "explanation": "extremely detailed explanation here",
                  "practiceProblems": [
                    "problem 1",
                    "problem 2",
                    "problem 3"
                  ]
                }

                Explanation requirements:
                - Explain the topic in detail.
                - Cover definition, intuition, why it matters, main components, step-by-step working, examples, use cases, edge cases, common mistakes, and exam/coding perspective.
                - Use beginner-friendly language.
                - If the topic is programming-related, include logical flow and small conceptual examples.
                - If the topic is math-related, explain meaning, formula intuition, steps, and common confusions.
                - Make explanation useful enough that a weak student can understand it.
                - Follow the extra user instruction if provided.

                Practice problem rules:
                - Generate exactly 3 practice problems.
                - Practice problems should match topic and difficulty.
                """.formatted(topic, difficulty, customPrompt);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 6000,
                        "responseMimeType", "application/json"
                )
        );

        String jsonRequest = objectMapper.writeValueAsString(requestBody);
        String responseBody = sendRequest(jsonRequest);
        String aiText = extractText(responseBody);
        String cleanJson = extractJsonObject(aiText);

        return objectMapper.readValue(
                cleanJson,
                new TypeReference<Map<String, Object>>() {}
        );
    }

    private List<Map<String, String>> generateMcqBatchWithRetry(
            String topic,
            String difficulty,
            String customPrompt,
            int batchSize,
            int batchNumber
    ) throws Exception {

        int maxRetries = 3;
        Exception lastError = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return generateMcqBatch(
                        topic,
                        difficulty,
                        customPrompt,
                        batchSize,
                        batchNumber,
                        attempt
                );
            } catch (Exception e) {
                lastError = e;
                System.out.println("MCQ batch failed. Batch: " + batchNumber + ", Attempt: " + attempt);
                System.out.println("Reason: " + e.getMessage());

                Thread.sleep(2000L);
            }
        }

        throw new RuntimeException("Failed to generate MCQ batch after retries: " + lastError.getMessage());
    }

    private List<Map<String, String>> generateMcqBatch(
            String topic,
            String difficulty,
            String customPrompt,
            int batchSize,
            int batchNumber,
            int attempt
    ) throws Exception {

        String prompt = """
                You are an expert quiz generator for software engineering students.

                Topic: %s
                Difficulty: %s
                Extra user instruction: %s
                Batch number: %d
                Attempt number: %d
                Number of MCQs required in this batch: %d

                Return ONLY valid JSON.
                Your first character must be { and your last character must be }.
                Do not write anything before or after JSON.
                Do not use markdown.
                Do not use ```json.

                Use this exact JSON structure:

                {
                  "mcqs": [
                    {
                      "question": "question text",
                      "optionA": "option A",
                      "optionB": "option B",
                      "optionC": "option C",
                      "optionD": "option D",
                      "correctAnswer": "A",
                      "weakArea": "specific weak area"
                    }
                  ]
                }

                Rules:
                - Generate exactly %d MCQs.
                - correctAnswer must be only A, B, C, or D.
                - Every MCQ must have one specific weakArea.
                - Weak areas should be precise, not generic.
                - Questions must not repeat.
                - Questions should match the selected difficulty.
                - Follow the extra user instruction if provided.
                - Keep each question concise.
                - Keep each option concise.
                - Do not include explanations inside MCQs.
                """.formatted(
                topic,
                difficulty,
                customPrompt,
                batchNumber,
                attempt,
                batchSize,
                batchSize
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 3000,
                        "responseMimeType", "application/json"
                )
        );

        String jsonRequest = objectMapper.writeValueAsString(requestBody);
        String responseBody = sendRequest(jsonRequest);
        String aiText = extractText(responseBody);
        String cleanJson = extractJsonObject(aiText);

        Map<String, Object> mcqData = objectMapper.readValue(
                cleanJson,
                new TypeReference<Map<String, Object>>() {}
        );

        List<Map<String, String>> mcqs = (List<Map<String, String>>) mcqData.get("mcqs");

        if (mcqs == null || mcqs.size() != batchSize) {
            throw new RuntimeException("Gemini returned wrong MCQ count. Expected "
                    + batchSize + " but got " + (mcqs == null ? 0 : mcqs.size()));
        }

        return mcqs;
    }

    private List<String> generatePracticeBatch(List<String> focusAreas, int batchNumber) throws Exception {

        String prompt = """
                You are an expert AI study coach.

                Generate personalized practice questions for weak areas.

                Batch number: %d
                Weak areas in order:
                %s

                Return ONLY valid JSON.
                Your first character must be { and your last character must be }.
                Do not write anything before or after JSON.
                Do not use markdown.

                Use this exact JSON structure:

                {
                  "practiceProblems": [
                    "practice question 1",
                    "practice question 2"
                  ]
                }

                Rules:
                - Generate exactly %d practice questions.
                - Generate one practice question for each weak area in the given order.
                - Each question must clearly target its weak area.
                - Keep questions beginner-friendly but useful.
                - Do not include answers.
                """.formatted(
                batchNumber,
                String.join("\n", focusAreas),
                focusAreas.size()
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 2500,
                        "responseMimeType", "application/json"
                )
        );

        String jsonRequest = objectMapper.writeValueAsString(requestBody);
        String responseBody = sendRequest(jsonRequest);
        String aiText = extractText(responseBody);
        String cleanJson = extractJsonObject(aiText);

        Map<String, Object> practiceData = objectMapper.readValue(
                cleanJson,
                new TypeReference<Map<String, Object>>() {}
        );

        List<String> practiceProblems = (List<String>) practiceData.get("practiceProblems");

        if (practiceProblems == null || practiceProblems.size() != focusAreas.size()) {
            throw new RuntimeException("Gemini returned wrong practice question count.");
        }

        return practiceProblems;
    }

    private String sendRequest(String jsonRequest) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GEMINI_API_KEY is missing");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model
                + ":generateContent";

        HttpClient client = HttpClient.newHttpClient();

        int maxRetries = 5;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            if (response.statusCode() == 503 || response.statusCode() == 429) {
                System.out.println("Gemini busy or rate limited. Attempt "
                        + attempt + " of " + maxRetries);

                long waitTime = attempt * 3000L;
                Thread.sleep(waitTime);
                continue;
            }

            throw new RuntimeException("Gemini error: " + response.body());
        }

        throw new RuntimeException("Gemini is busy right now. Please try again after a few minutes.");
    }

    private String extractText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode textNode = root
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("No text found in Gemini response");
        }

        return textNode.asText().trim();
    }

    private String extractJsonObject(String aiText) {
        String cleaned = aiText
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int firstBrace = cleaned.indexOf("{");
        int lastBrace = cleaned.lastIndexOf("}");

        if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
            throw new RuntimeException("Gemini did not return valid JSON. Response was: " + cleaned);
        }

        return cleaned.substring(firstBrace, lastBrace + 1);
    }
}