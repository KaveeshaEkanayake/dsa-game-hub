package com.dsagamehub.repository;

import com.dsagamehub.model.KnightTourResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnightTourRepository extends JpaRepository<KnightTourResult, Long> {
    List<KnightTourResult> findByPlayerName(String playerName);
    List<KnightTourResult> findByBoardSize(int boardSize);
    List<KnightTourResult> findByIsCorrectTrue();
}