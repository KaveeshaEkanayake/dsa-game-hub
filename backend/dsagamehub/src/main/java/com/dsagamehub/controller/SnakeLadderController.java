package com.dsagamehub.controller;

import com.dsagamehub.dto.*;
import com.dsagamehub.service.SnakeLadderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/snake-ladder")
@CrossOrigin("*")
public class SnakeLadderController {

    private final SnakeLadderService service;

    public SnakeLadderController(SnakeLadderService service) {
        this.service = service;
    }

    // 🎮 START GAME
    @PostMapping("/start")
    public SnakeLadderResponse start(@RequestBody SnakeLadderRequest request) {
        return service.startGame(request);
    }

    // 🎯 SUBMIT ANSWER (USE COMMON DTO)
    @PostMapping("/submit")
    public ApiResponse submit(@RequestBody PlayerAnswerRequest request) {
        return service.submitAnswer(request);
    }
}