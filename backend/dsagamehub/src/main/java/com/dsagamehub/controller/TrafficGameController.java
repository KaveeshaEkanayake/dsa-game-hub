package com.dsagamehub.controller;

import com.dsagamehub.exception.TrafficGameException;
import com.dsagamehub.model.*;
import com.dsagamehub.service.TrafficGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
@CrossOrigin(origins = "*")
public class TrafficGameController {

    @Autowired
    private TrafficGameService trafficGameService;

    @GetMapping("/new-round")
    public ResponseEntity<?> newRound() {
        try {
            TrafficGameResponse response = trafficGameService.generateNewRound();
            return ResponseEntity.ok(response);
        } catch (TrafficGameException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(buildErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("SERVER_ERROR", "Something went wrong"));
        }
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<?> submitAnswer(@RequestBody TrafficAnswerRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(buildErrorResponse("INVALID_REQUEST", "Request body cannot be empty"));
            }

            TrafficGameResponse response = trafficGameService.submitAnswer(request);
            return ResponseEntity.ok(response);

        } catch (TrafficGameException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(buildErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("SERVER_ERROR", "Something went wrong"));
        }
    }

    @GetMapping("/results")
    public ResponseEntity<?> getCorrectAnswers() {
        try {
            List<TrafficGameResult> results = trafficGameService.getCorrectAnswers();
            return ResponseEntity.ok(results);
        } catch (TrafficGameException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(buildErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("SERVER_ERROR", "Something went wrong"));
        }
    }

    @GetMapping("/results/{roundId}")
    public ResponseEntity<?> getResultsByRound(@PathVariable Long roundId) {
        try {
            if (roundId == null || roundId <= 0) {
                return ResponseEntity.badRequest()
                        .body(buildErrorResponse("INVALID_ROUND_ID", "Round ID must be a positive number"));
            }

            List<TrafficGameResult> results = trafficGameService.getResultsByRound(roundId);
            return ResponseEntity.ok(results);

        } catch (TrafficGameException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(buildErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("SERVER_ERROR", "Something went wrong"));
        }
    }

    @GetMapping("/rounds")
    public ResponseEntity<?> getAllRounds() {
        try {
            List<TrafficGameRound> rounds = trafficGameService.getAllRounds();
            return ResponseEntity.ok(rounds);
        } catch (TrafficGameException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(buildErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("SERVER_ERROR", "Something went wrong"));
        }
    }

    private Map<String, String> buildErrorResponse(String errorCode, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("errorCode", errorCode);
        error.put("message", message);
        return error;
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        try {
            List<TrafficGameResult> results = trafficGameService.getLeaderboard();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("SERVER_ERROR", "Something went wrong"));
        }
    }
}