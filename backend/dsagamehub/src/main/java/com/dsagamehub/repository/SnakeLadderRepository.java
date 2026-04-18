package com.dsagamehub.repository;

import com.dsagamehub.model.SnakeLadderGameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SnakeLadderRepository extends JpaRepository<SnakeLadderGameResult, Long> {

    // ✅ Use 'playerName' field (matches entity)
    List<SnakeLadderGameResult> findByPlayerName(String playerName);

    // ✅ Use 'isWin' field (matches entity)
    List<SnakeLadderGameResult> findByIsWin(boolean isWin);

    // ✅ Count by player name and win status
    long countByPlayerNameAndIsWin(String playerName, boolean isWin);
}