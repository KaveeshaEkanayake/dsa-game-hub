package com.dsagamehub.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SixteenQueensValidationService {

    public String normalizeAnswer(String answer) {
        return answer.trim().replaceAll("\\s+", "");
    }

    public boolean isValidFormat(String answer) {
        String[] parts = answer.split(",");

        if (parts.length != 16) {
            return false;
        }

        Set<Integer> usedColumns = new HashSet<>();

        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);

                if (value < 0 || value > 15) {
                    return false;
                }

                if (usedColumns.contains(value)) {
                    return false;
                }

                usedColumns.add(value);

            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    public int[] parseBoard(String answer) {
        String[] parts = answer.split(",");
        int[] board = new int[16];

        for (int i = 0; i < 16; i++) {
            board[i] = Integer.parseInt(parts[i]);
        }

        return board;
    }

    public boolean isSelfConflictFree(String answer) {
        int[] board = parseBoard(answer);

        for (int i = 0; i < 16; i++) {
            for (int j = i + 1; j < 16; j++) {
                if (board[i] == board[j]) {
                    return false;
                }

                if (Math.abs(board[i] - board[j]) == Math.abs(i - j)) {
                    return false;
                }
            }
        }

        return true;
    }
}
