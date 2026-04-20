package com.dsagamehub.controller;

import com.dsagamehub.dto.MinimumCostRoundRequest;
import com.dsagamehub.dto.MinimumCostRoundResponse;
import com.dsagamehub.model.MinimumCostRound;
import com.dsagamehub.service.MinimumCostGameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/minimum-cost")
@CrossOrigin(origins = "http://localhost:5173")
public class MinimumCostController {

    private final MinimumCostGameService minimumCostGameService;

    public MinimumCostController(MinimumCostGameService minimumCostGameService) {
        this.minimumCostGameService = minimumCostGameService;
    }

    @PostMapping("/run-round/random")
    public MinimumCostRoundResponse runRandomRound() {
        return minimumCostGameService.runRandomRound();
    }

    @PostMapping("/run-round")
    public MinimumCostRoundResponse runRound(@Valid @RequestBody MinimumCostRoundRequest request) {
        return minimumCostGameService.runRound(request.getTaskCount());
    }

    @GetMapping("/history")
    public List<MinimumCostRound> getHistory() {
        return minimumCostGameService.getHistory();
    }
}