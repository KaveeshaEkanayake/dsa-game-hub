package com.dsagamehub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/minimum-cost")
public class MinimumCostController {

    @GetMapping
    public void getPage() {
        return;
    }
}