package com.ahmed.studycoach.repository;

import com.ahmed.studycoach.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findAllByOrderByCreatedAtDesc();
}