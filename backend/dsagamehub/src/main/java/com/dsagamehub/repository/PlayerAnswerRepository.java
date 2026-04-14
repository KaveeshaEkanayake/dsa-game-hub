package com.dsagamehub.repository;

import com.dsagamehub.model.PlayerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long> {

    Optional<PlayerAnswer> findByAnswerTextAndCorrectTrue(String answerText);

    List<PlayerAnswer> findByCorrectTrue();

    @Query("SELECT COUNT(DISTINCT p.answerText) FROM PlayerAnswer p WHERE p.correct = true")
    long countDistinctCorrectAnswers();
}