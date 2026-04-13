package com.dsagamehub.repository;

import com.dsagamehub.model.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {
}