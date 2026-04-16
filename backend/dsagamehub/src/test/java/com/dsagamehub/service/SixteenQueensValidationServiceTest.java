package com.dsagamehub.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SixteenQueensValidationServiceTest {

    private final SixteenQueensValidationService service = new SixteenQueensValidationService();

    @Test
    void shouldValidateCorrectFormat() {
        String answer = "0,4,7,5,2,6,1,3,8,10,12,14,9,11,13,15";
        assertTrue(service.isValidFormat(answer));
    }

    @Test
    void shouldRejectWrongFormat() {
        String answer = "0,1,2";
        assertFalse(service.isValidFormat(answer));
    }
}