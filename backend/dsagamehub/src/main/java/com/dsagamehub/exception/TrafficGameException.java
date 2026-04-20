package com.dsagamehub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TrafficGameException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public TrafficGameException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public TrafficGameException(String message) {
        super(message);
        this.errorCode = "TRAFFIC_GAME_ERROR";
        this.status = HttpStatus.BAD_REQUEST;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }

    public static TrafficGameException invalidPlayerName() {
        return new TrafficGameException(
                "Player name cannot be empty or null",
                "INVALID_PLAYER_NAME",
                HttpStatus.BAD_REQUEST
        );
    }

    public static TrafficGameException invalidAnswer() {
        return new TrafficGameException(
                "Player answer must be a positive number",
                "INVALID_ANSWER",
                HttpStatus.BAD_REQUEST
        );
    }

    public static TrafficGameException roundNotFound(Long roundId) {
        return new TrafficGameException(
                "Game round not found with id: " + roundId,
                "ROUND_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public static TrafficGameException algorithmFailure() {
        return new TrafficGameException(
                "Algorithm failed to compute max flow",
                "ALGORITHM_FAILURE",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}