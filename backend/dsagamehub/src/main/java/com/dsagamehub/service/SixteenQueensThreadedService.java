package com.dsagamehub.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class SixteenQueensThreadedService {

    private static final int SIZE = 16;

    public long countQueensThreaded() {
        int threadCount = Math.min(SIZE, Runtime.getRuntime().availableProcessors());
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<Long>> futureList = new ArrayList<>();

        for (int firstCol = 0; firstCol < SIZE; firstCol++) {
            final int col = firstCol;

            Callable<Long> task = () -> {
                int[] board = new int[SIZE];
                board[0] = col;
                return solveRow(1, board);
            };

            futureList.add(executorService.submit(task));
        }

        long totalCount = 0;

        for (Future<Long> future : futureList) {
            try {
                totalCount += future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while counting solutions", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Error while counting threaded solutions", e);
            }
        }

        executorService.shutdown();
        return totalCount;
    }

    private long solveRow(int row, int[] board) {
        if (row == SIZE) {
            return 1;
        }

        long count = 0;

        for (int col = 0; col < SIZE; col++) {
            if (isSafe(row, col, board)) {
                board[row] = col;
                count += solveRow(row + 1, board);
            }
        }

        return count;
    }

    private boolean isSafe(int row, int col, int[] board) {
        for (int i = 0; i < row; i++) {
            int existingCol = board[i];

            if (existingCol == col) {
                return false;
            }

            if (Math.abs(existingCol - col) == Math.abs(i - row)) {
                return false;
            }
        }

        return true;
    }
}