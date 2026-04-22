package com.dsagamehub.repository;

import com.dsagamehub.model.KnightTourResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnightTourRepository extends JpaRepository<KnightTourResult, Long> {
    List<KnightTourResult> findByPlayerName(String playerName);
    List<KnightTourResult> findByBoardSize(int boardSize);
    List<KnightTourResult> findByIsCorrectTrue();
    List<KnightTourResult> findByGameResult(String gameResult);

    @Query("SELECT r FROM KnightTourResult r WHERE r.isCorrect = true ORDER BY r.createdAt DESC")
    List<KnightTourResult> findTopCorrectResults();

    @Query("SELECT r FROM KnightTourResult r ORDER BY r.createdAt DESC")
    List<KnightTourResult> findAllOrderByCreatedAtDesc();
}