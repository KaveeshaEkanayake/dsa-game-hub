package com.dsagamehub.repository;

import com.dsagamehub.model.MinimumCostRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MinimumCostRoundRepository extends JpaRepository<MinimumCostRound, Long> {

    Optional<MinimumCostRound> findTopByOrderByRoundNumberDesc();

    List<MinimumCostRound> findAllByOrderByCreatedAtDesc();
}