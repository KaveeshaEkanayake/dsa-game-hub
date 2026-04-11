package com.dsagamehub.controller;

import com.dsagamehub.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/minimum-cost")
public class MinimumCostController {

    @GetMapping
    public ApiResponse getPage() {
        return new ApiResponse("Minimum Cost backend placeholder", null);
    }
}