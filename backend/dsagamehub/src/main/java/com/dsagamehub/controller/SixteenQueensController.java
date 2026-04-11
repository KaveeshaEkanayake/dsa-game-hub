package com.dsagamehub.controller;

import com.dsagamehub.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sixteen-queens")
public class SixteenQueensController {

    @GetMapping
    public ApiResponse getPage() {
        return new ApiResponse("Sixteen Queens backend placeholder", null);
    }
}