package com.ahmed.studycoach.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    private String difficulty;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String practiceProblems;

    private LocalDateTime createdAt;

    public StudySession() {
    }

    public StudySession(String topic, String difficulty, String summary, String explanation, String practiceProblems) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.summary = summary;
        this.explanation = explanation;
        this.practiceProblems = practiceProblems;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getSummary() {
        return summary;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getPracticeProblems() {
        return practiceProblems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}