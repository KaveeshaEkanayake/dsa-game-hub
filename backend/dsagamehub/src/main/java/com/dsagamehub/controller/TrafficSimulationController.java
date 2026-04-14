package com.dsagamehub.controller;

import com.dsagamehub.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traffic-simulation")
public class TrafficSimulationController {

    @GetMapping
    public void getPage() {
        return;
    }
}