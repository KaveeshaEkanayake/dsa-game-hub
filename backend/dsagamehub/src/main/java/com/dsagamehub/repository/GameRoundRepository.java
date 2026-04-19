package com.dsagamehub.repository;

import com.dsagamehub.model.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    Optional<GameRound> findTopByGameNameOrderByRoundNumberDesc(String gameName);

    List<GameRound> findByGameNameOrderByRoundNumberDesc(String gameName);
}