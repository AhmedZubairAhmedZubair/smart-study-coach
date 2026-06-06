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
                        )
                ),

                "practiceProblems", List.of(
                        "Explain " + topic + " in your own words.",
                        "Write one real-life example of " + topic + ".",
                        "Create a simple program or diagram related to " + topic + "."
                )
        );
    }
}