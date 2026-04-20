package com.dsagamehub.controller;

import com.dsagamehub.dto.ApiResponse;
import com.dsagamehub.dto.PlayerAnswerRequest;
import com.dsagamehub.dto.SnakeLadderRequest;
import com.dsagamehub.dto.SnakeLadderResponse;
import com.dsagamehub.service.SnakeLadderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/snake-ladder")
public class SnakeLadderController {

    private final SnakeLadderService service;

    public SnakeLadderController(SnakeLadderService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public SnakeLadderResponse start(@RequestBody SnakeLadderRequest request) {
        return service.startGame(request);
    }

    @PostMapping("/submit")
    public ApiResponse submit(@RequestBody PlayerAnswerRequest request) {
        return service.submitAnswer(request);
    }

    @GetMapping
    public String health() {
        return "Snake Ladder API is running";
    }
}