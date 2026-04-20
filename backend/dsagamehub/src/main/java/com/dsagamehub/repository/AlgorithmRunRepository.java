package com.dsagamehub.repository;

import com.dsagamehub.model.AlgorithmRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlgorithmRunRepository extends JpaRepository<AlgorithmRun, Long> {
    List<AlgorithmRun> findByGameNameOrderByRoundNumberDescCreatedAtDesc(String gameName);

    List<AlgorithmRun> findByGameNameAndRoundNumberOrderByCreatedAtAsc(String gameName, int roundNumber);

    Optional<AlgorithmRun> findTopByGameNameOrderBySolutionCountDescCreatedAtDesc(String gameName);

    List<AlgorithmRun> findByGameRoundId(Long gameRoundId);

    // ADD THIS METHOD
    @Query("SELECT a FROM AlgorithmRun a WHERE a.gameRoundId = :roundId AND a.algorithmType = :type")
    AlgorithmRun findByGameRoundIdAndAlgorithmType(@Param("roundId") Long roundId, @Param("type") String type);
}
