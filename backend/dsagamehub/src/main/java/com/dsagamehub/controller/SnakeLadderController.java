package com.dsagamehub.controller;

import com.dsagamehub.dto.*;
import com.dsagamehub.service.SnakeLadderService;
import org.springframework.web.bind.annotation.*;

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
}