package com.dsagamehub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MinimumCostRoundRequest {

    @NotNull(message = "Task count is required.")
    @Min(value = 50, message = "Task count must be at least 50.")
    @Max(value = 100, message = "Task count must be at most 100.")
    private Integer taskCount;

    public MinimumCostRoundRequest() {
    }

    public MinimumCostRoundRequest(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }
}