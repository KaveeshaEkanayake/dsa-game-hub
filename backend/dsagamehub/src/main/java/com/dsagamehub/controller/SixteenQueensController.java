package com.dsagamehub.controller;

import com.dsagamehub.dto.AlgorithmRunResponse;
import com.dsagamehub.dto.ApiResponse;
import com.dsagamehub.dto.PlayerAnswerRequest;
import com.dsagamehub.model.AlgorithmRun;
import com.dsagamehub.model.PlayerAnswer;
import com.dsagamehub.service.SixteenQueensGameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sixteen-queens")
@CrossOrigin(origins = "http://localhost:5173")
public class SixteenQueensController {

    private final SixteenQueensGameService gameService;

    public SixteenQueensController(SixteenQueensGameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/run-sequential")
    public AlgorithmRunResponse runSequential() {
        return gameService.runSequential();
    }

    @PostMapping("/run-threaded")
    public AlgorithmRunResponse runThreaded() {
        return gameService.runThreaded();
    }

    @PostMapping("/submit-answer")
    public ApiResponse submitAnswer(@Valid @RequestBody PlayerAnswerRequest request) {
        return gameService.submitAnswer(request);
    }

    @GetMapping("/results")
    public List<AlgorithmRun> getResults() {
        return gameService.getAllRuns();
    }

    @GetMapping("/answers")
    public List<PlayerAnswer> getAnswers() {
        return gameService.getAllPlayerAnswers();
    }

    @PostMapping("/reset")
    public ApiResponse resetRecognizedFlags() {
        return gameService.resetRecognizedAnswers();
    }
}