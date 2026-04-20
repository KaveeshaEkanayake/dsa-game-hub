/*package com.dsagamehub.repository;

import com.dsagamehub.model.SnakeLadderGameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SnakeLadderRepository extends JpaRepository<SnakeLadderGameResult, Long> {

    // Use 'playerName' field (matches entity)
    List<SnakeLadderGameResult> findByPlayerName(String playerName);

    // Use 'isWin' field (matches entity)
    List<SnakeLadderGameResult> findByIsWin(boolean isWin);

    // Count by player name and win status
    long countByPlayerNameAndIsWin(String playerName, boolean isWin);
}*/

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
}