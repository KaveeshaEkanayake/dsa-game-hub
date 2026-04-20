package com.dsagamehub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knights-tour")
public class KnightsTourController {

    @GetMapping
    public void getPage() {
        return;
    }
}