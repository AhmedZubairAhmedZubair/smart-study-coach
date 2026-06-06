package com.ahmed.studycoach.controller;

import com.ahmed.studycoach.model.QuizAttempt;
import com.ahmed.studycoach.model.StudySession;
import com.ahmed.studycoach.repository.QuizAttemptRepository;
import com.ahmed.studycoach.repository.StudySessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
@CrossOrigin("*")
public class StudyController {

    private final StudySessionRepository studySessionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public StudyController(
            StudySessionRepository studySessionRepository,
            QuizAttemptRepository quizAttemptRepository
    ) {
        this.studySessionRepository = studySessionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @PostMapping("/generate")
    public Map<String, Object> generateStudyContent(@RequestBody Map<String, String> request) {

        String topic = request.get("topic");
        String difficulty = request.get("difficulty");

        String summary = topic + " is an important topic for software engineering students.";

        String explanation = "First understand the basic meaning of " + topic +
                ". Then learn its main parts. After that, solve small examples.";

        List<Map<String, String>> mcqs = List.of(
                Map.of(
                        "question", "What is " + topic + " mainly used for?",
                        "optionA", "Learning and solving computer science problems",
                        "optionB", "Editing videos",
                        "optionC", "Playing games",
                        "optionD", "Browsing websites",
                        "correctAnswer", "A",
                        "weakArea", topic + " basic concept"
                ),
                Map.of(
                        "question", "What is the best way to understand " + topic + "?",
                        "optionA", "Only memorize definitions",
                        "optionB", "Understand concept and solve examples",
                        "optionC", "Skip difficult parts",
                        "optionD", "Only watch videos",
                        "correctAnswer", "B",
                        "weakArea", topic + " understanding"
                ),
                Map.of(
                        "question", "Why is practice important in " + topic + "?",
                        "optionA", "It makes concepts stronger",
                        "optionB", "It wastes time",
                        "optionC", "It removes the need to learn theory",
                        "optionD", "It is only for exams",
                        "correctAnswer", "A",
                        "weakArea", topic + " practice"
                ),
                Map.of(
                        "question", "What should a student do after learning basics of " + topic + "?",
                        "optionA", "Stop studying",
                        "optionB", "Solve small problems",
                        "optionC", "Ignore examples",
                        "optionD", "Only copy notes",
                        "correctAnswer", "B",
                        "weakArea", topic + " problem solving"
                ),
                Map.of(
                        "question", "Which mistake should students avoid while learning " + topic + "?",
                        "optionA", "Practicing examples",
                        "optionB", "Asking questions",
                        "optionC", "Only memorizing without understanding",
                        "optionD", "Making notes",
                        "correctAnswer", "C",
                        "weakArea", topic + " common mistakes"
                )
        );

        List<String> practiceProblems = List.of(
                "Explain " + topic + " in your own words.",
                "Write one real-life example of " + topic + ".",
                "Create a simple program or diagram related to " + topic + "."
        );

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
    public Map<String, Object> generatePersonalizedPractice(@RequestBody Map<String, String> request) {

        String weakArea = request.get("weakArea");

        return Map.of(
                "weakArea", weakArea,
                "practiceProblems", List.of(
                        "Explain the concept of " + weakArea + " in simple words.",
                        "Write 3 key points about " + weakArea + ".",
                        "Create a small example related to " + weakArea + ".",
                        "Solve one easy question based on " + weakArea + ".",
                        "Write one common mistake students make in " + weakArea + "."
                )
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
}