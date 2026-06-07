package com.ahmed.studycoach.controller;

import com.ahmed.studycoach.model.QuizAttempt;
import com.ahmed.studycoach.model.StudySession;
import com.ahmed.studycoach.repository.QuizAttemptRepository;
import com.ahmed.studycoach.repository.StudySessionRepository;
import com.ahmed.studycoach.service.GeminiService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/study")
@CrossOrigin("*")
public class StudyController {

    private final StudySessionRepository studySessionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final GeminiService geminiService;

    public StudyController(
            StudySessionRepository studySessionRepository,
            QuizAttemptRepository quizAttemptRepository,
            GeminiService geminiService
    ) {
        this.studySessionRepository = studySessionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.geminiService = geminiService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generateStudyContent(@RequestBody Map<String, String> request) {

        String topic = request.get("topic");
        String difficulty = request.get("difficulty");
        int mcqCount = Integer.parseInt(request.getOrDefault("mcqCount", "5"));
        String customPrompt = request.getOrDefault("customPrompt", "");

        Map<String, Object> aiData = geminiService.generateStudyContent(
                topic,
                difficulty,
                mcqCount,
                customPrompt
        );

        String summary = aiData.get("summary").toString();
        String explanation = aiData.get("explanation").toString();

        List<Map<String, String>> mcqs = (List<Map<String, String>>) aiData.get("mcqs");
        List<String> practiceProblems = (List<String>) aiData.get("practiceProblems");

        StudySession session = new StudySession(
                topic,
                difficulty,
                summary,
                explanation,
                String.join(" | ", practiceProblems)
        );

        StudySession savedSession = studySessionRepository.save(session);

        return Map.of(
                "sessionId", savedSession.getId(),
                "topic", topic,
                "difficulty", difficulty,
                "summary", summary,
                "explanation", explanation,
                "mcqs", mcqs,
                "practiceProblems", practiceProblems
        );
    }

    @PostMapping("/personalized-practice")
    public Map<String, Object> generatePersonalizedPractice(@RequestBody Map<String, Object> request) {

        List<String> weakAreas = new ArrayList<>();

        Object weakAreasObject = request.get("weakAreas");

        if (weakAreasObject instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) {
                    weakAreas.add(item.toString());
                }
            }
        }

        if (weakAreas.isEmpty()) {
            return Map.of(
                    "totalWrong", 0,
                    "priorityTopics", List.of(),
                    "practiceProblems", List.of("No weak area detected. Great job!")
            );
        }

        PriorityQueue<WeakTopic> priorityQueue = buildWeakTopicPriorityQueue(weakAreas);

        List<Map<String, Object>> priorityTopics = new ArrayList<>();

        while (!priorityQueue.isEmpty()) {
            WeakTopic weakTopic = priorityQueue.poll();

            priorityTopics.add(Map.of(
                    "topic", weakTopic.topic,
                    "count", weakTopic.count
            ));
        }

        List<String> practiceProblems = geminiService.generatePriorityBasedPractice(priorityTopics);

        return Map.of(
                "totalWrong", weakAreas.size(),
                "priorityTopics", priorityTopics,
                "practiceProblems", practiceProblems
        );
    }

    @PostMapping("/quiz-result")
    public Map<String, Object> saveQuizResult(@RequestBody Map<String, String> request) {

        Long sessionId = Long.parseLong(request.get("sessionId"));
        int score = Integer.parseInt(request.get("score"));
        int totalQuestions = Integer.parseInt(request.get("totalQuestions"));
        String weakArea = request.get("weakArea");

        QuizAttempt quizAttempt = new QuizAttempt(
                sessionId,
                score,
                totalQuestions,
                weakArea
        );

        QuizAttempt savedAttempt = quizAttemptRepository.save(quizAttempt);

        return Map.of(
                "message", "Quiz result saved successfully",
                "attemptId", savedAttempt.getId()
        );
    }

    @GetMapping("/history")
    public List<StudySession> getStudyHistory() {
        return studySessionRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/quiz-history")
    public List<QuizAttempt> getQuizHistory() {
        return quizAttemptRepository.findAllByOrderByCreatedAtDesc();
    }

    private PriorityQueue<WeakTopic> buildWeakTopicPriorityQueue(List<String> weakAreas) {

        Map<String, Integer> frequencyMap = new HashMap<>();

        for (String area : weakAreas) {
            frequencyMap.put(area, frequencyMap.getOrDefault(area, 0) + 1);
        }

        PriorityQueue<WeakTopic> priorityQueue = new PriorityQueue<>(
                (a, b) -> b.count - a.count
        );

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new WeakTopic(entry.getKey(), entry.getValue()));
        }

        return priorityQueue;
    }

    private static class WeakTopic {
        String topic;
        int count;

        WeakTopic(String topic, int count) {
            this.topic = topic;
            this.count = count;
        }
    }
}