package com.dsagamehub.repository;

import com.dsagamehub.model.AlgorithmRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlgorithmRunRepository extends JpaRepository<AlgorithmRun, Long> {
    List<AlgorithmRun> findByGameNameOrderByCreatedAtDesc(String gameName);

    Optional<AlgorithmRun> findTopByGameNameOrderBySolutionCountDescCreatedAtDesc(String gameName);
}
