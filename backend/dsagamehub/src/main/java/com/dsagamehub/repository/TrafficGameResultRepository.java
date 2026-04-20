package com.dsagamehub.repository;

import com.dsagamehub.model.TrafficGameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrafficGameResultRepository extends JpaRepository<TrafficGameResult, Long> {

    List<TrafficGameResult> findByRoundId(Long roundId);

    List<TrafficGameResult> findByPlayerName(String playerName);

    List<TrafficGameResult> findByIsCorrectTrue();

    List<TrafficGameResult> findAllByOrderByCreatedAtDesc();

}