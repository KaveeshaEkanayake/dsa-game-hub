package com.dsagamehub.controller;

import com.dsagamehub.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knights-tour")
public class KnightsTourController {

    @GetMapping
    public ApiResponse getPage() {
        return new ApiResponse("Knight's Tour backend placeholder", null);
    }
}