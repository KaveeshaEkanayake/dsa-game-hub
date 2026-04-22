package com.dsagamehub.controller;

import com.dsagamehub.dto.*;
import com.dsagamehub.model.SnakeLadderGameResult;
import com.dsagamehub.service.SnakeLadderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snake-ladder")
@CrossOrigin(origins = "*")

public class SnakeLadderController {

    private final SnakeLadderService service;

    public SnakeLadderController(SnakeLadderService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public SnakeLadderResponse startGame(@RequestBody SnakeLadderRequest request) {
        return service.startGame(request);
    }

    @PostMapping("/submit")
    public SnakeLadderApiResponse submitAnswer(@RequestBody SnakeLadderAnswerRequest request) {
        return service.submitAnswer(request);
    }


    @GetMapping("/performance/history")
    public List<PerformanceRecord> getPerformanceHistory(@RequestParam(required = false) String playerName) {
        return service.getPerformanceHistoryForPlayer(playerName);
    }

    @GetMapping("/performance/stats")
    public Map<String, Object> getPerformanceStats(@RequestParam(required = false) String playerName) {
        return service.getPerformanceStatsForPlayer(playerName);
    }
}