package com.ahmed.studycoach.repository;

import com.ahmed.studycoach.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findAllByOrderByCreatedAtDesc();
}