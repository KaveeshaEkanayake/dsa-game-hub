package com.dsagamehub.repository;

import com.dsagamehub.model.SnakeLadderGameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SnakeLadderRepository extends JpaRepository<SnakeLadderGameResult, Long> {

    List<SnakeLadderGameResult> findByPlayerName(String playerName);
    List<SnakeLadderGameResult> findByIsWin(boolean isWin);
    long countByPlayerNameAndIsWin(String playerName, boolean isWin);

    @Query("SELECT s FROM SnakeLadderGameResult s WHERE s.gameRoundId = :roundId")
    List<SnakeLadderGameResult> findByGameRoundId(@Param("roundId") Long roundId);

    @Query("SELECT DISTINCT s.playerName FROM SnakeLadderGameResult s WHERE s.isWin = true")
    List<String> findWinningPlayers();

    //Get last 20 results for performance chart
    @Query(value = "SELECT * FROM snake_ladder_game_result ORDER BY played_at DESC LIMIT 20",
            nativeQuery = true)
    List<SnakeLadderGameResult> findLast20Results();

    //Get results by player with limit
    @Query(value = "SELECT * FROM snake_ladder_game_result WHERE player_name = :playerName ORDER BY played_at DESC LIMIT 20",
            nativeQuery = true)
    List<SnakeLadderGameResult> findLast20ResultsByPlayer(@Param("playerName") String playerName);
}