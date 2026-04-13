package com.dsagamehub.controller;

import com.dsagamehub.service.KnightTourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/knights-tour")
@CrossOrigin(origins = "http://localhost:5173")
public class KnightTourController {

    @Autowired
    private KnightTourService knightTourService;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestParam int boardSize) {
        return ResponseEntity.ok(knightTourService.generateGame(boardSize));
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(knightTourService.submitAnswer(request));
    }
}