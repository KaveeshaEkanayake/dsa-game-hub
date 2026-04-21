package com.dsagamehub.controller;

import com.dsagamehub.service.KnightTourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/knights-tour")
public class KnightTourController {

    @Autowired
    private KnightTourService knightTourService;

    @GetMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam int boardSize) {
        try {
            if (boardSize != 8 && boardSize != 16) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Board size must be 8 or 16"));
            }
            return ResponseEntity.ok(knightTourService.generateGame(boardSize));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate game: " + e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> request) {
        try {
            // Validate required fields
            if (request.get("playerName") == null || request.get("playerName").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Player name is required"));
            }
            if (request.get("playerAnswer") == null || request.get("playerAnswer").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Player answer is required"));
            }
            if (request.get("boardSize") == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Board size is required"));
            }

            int boardSize = ((Number) request.get("boardSize")).intValue();
            if (boardSize != 8 && boardSize != 16) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Board size must be 8 or 16"));
            }

            // Validate player answer is a number
            try {
                Integer.parseInt(request.get("playerAnswer").toString().trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Player answer must be a valid number"));
            }

            return ResponseEntity.ok(knightTourService.submitAnswer(request));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to submit answer: " + e.getMessage()));
        }
    }

    @PostMapping("/draw")
    public ResponseEntity<?> draw(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("playerName") == null || request.get("playerName").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Player name is required"));
            }
            return ResponseEntity.ok(knightTourService.recordDraw(request));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to record draw: " + e.getMessage()));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> leaderboard() {
        try {
            return ResponseEntity.ok(knightTourService.getLeaderboard());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch leaderboard: " + e.getMessage()));
        }
    }

    @GetMapping("/results")
    public ResponseEntity<?> results() {
        try {
            return ResponseEntity.ok(knightTourService.getAllResults());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch results: " + e.getMessage()));
        }
    }
}