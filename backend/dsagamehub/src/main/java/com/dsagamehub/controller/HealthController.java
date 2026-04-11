package com.dsagamehub.controller;

import com.dsagamehub.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse health() {
        return new ApiResponse("Backend is running successfully", null);
    }
}