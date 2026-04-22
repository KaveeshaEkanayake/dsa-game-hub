package com.dsagamehub.repository;

import com.dsagamehub.model.PlayerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long> {

    Optional<PlayerAnswer> findByAnswerText(String answerText);

    List<PlayerAnswer> findAllByOrderBySubmittedAtDesc();

    long countByRoundNumber(int roundNumber);
}