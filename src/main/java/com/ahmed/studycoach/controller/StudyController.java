package com.ahmed.studycoach.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study")
@CrossOrigin("*")
public class StudyController {

    @PostMapping("/generate")
    public Map<String, Object> generateStudyContent(@RequestBody Map<String, String> request) {

        String topic = request.get("topic");
        String difficulty = request.get("difficulty");

        return Map.of(
                "topic", topic,
                "difficulty", difficulty,

                "summary", topic + " is an important topic for software engineering students.",

                "explanation", "First understand the basic meaning of " + topic +
                        ". Then learn its main parts. After that, solve small examples.",

                "mcqs", List.of(
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
                ),

                "practiceProblems", List.of(
                        "Explain " + topic + " in your own words.",
                        "Write one real-life example of " + topic + ".",
                        "Create a simple program or diagram related to " + topic + "."
                )
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
}