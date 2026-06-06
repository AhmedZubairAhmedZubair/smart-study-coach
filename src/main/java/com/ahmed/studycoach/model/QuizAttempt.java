package com.ahmed.studycoach.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;

    private int score;

    private int totalQuestions;

    @Column(length = 1000)
    private String weakArea;

    private LocalDateTime createdAt;

    public QuizAttempt() {
    }

    public QuizAttempt(Long sessionId, int score, int totalQuestions, String weakArea) {
        this.sessionId = sessionId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.weakArea = weakArea;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public String getWeakArea() {
        return weakArea;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}