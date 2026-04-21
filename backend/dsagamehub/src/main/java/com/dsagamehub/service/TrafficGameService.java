package com.dsagamehub.service;

import com.dsagamehub.exception.TrafficGameException;
import com.dsagamehub.model.*;
import com.dsagamehub.repository.TrafficGameResultRepository;
import com.dsagamehub.repository.TrafficGameRoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrafficGameService {

    @Autowired
    private TrafficGameRoundRepository roundRepository;

    @Autowired
    private TrafficGameResultRepository resultRepository;

    @Autowired
    private TrafficFordFulkersonService fordFulkersonService;

    @Autowired
    private TrafficEdmondsKarpService edmondsKarpService;

    private static final List<String[]> EDGES = Arrays.asList(
            new String[]{"A", "B"},
            new String[]{"A", "C"},
            new String[]{"A", "D"},
            new String[]{"B", "E"},
            new String[]{"B", "F"},
            new String[]{"C", "E"},
            new String[]{"C", "F"},
            new String[]{"D", "F"},
            new String[]{"E", "G"},
            new String[]{"E", "H"},
            new String[]{"F", "H"},
            new String[]{"G", "T"},
            new String[]{"H", "T"}
    );

//    public TrafficGameResponse generateNewRound() {
//        try {
//            List<TrafficEdge> edges = generateRandomEdges();
//
//            long ffStart = System.currentTimeMillis();
//            int ffMaxFlow = fordFulkersonService.computeMaxFlow(edges);
//            long ffTime = System.currentTimeMillis() - ffStart;
//
//            long ekStart = System.currentTimeMillis();
//            int ekMaxFlow = edmondsKarpService.computeMaxFlow(edges);
//            long ekTime = System.currentTimeMillis() - ekStart;
//
//            if (ffMaxFlow != ekMaxFlow) {
//                throw TrafficGameException.algorithmFailure();
//            }
//
//            TrafficGameRound round = new TrafficGameRound(ffMaxFlow, ffTime, ekTime);
//            round = roundRepository.save(round);
//
//            for (TrafficEdge edge : edges) {
//                edge.setRoundId(round.getId());
//            }
//
//            Map<String, Integer> capacities = buildCapacityMap(edges);
//
//            TrafficGameResponse response = new TrafficGameResponse(
//                    round.getId(), edges, capacities, ffMaxFlow, ffTime, ekTime
//            );
//
//            return response;
//
//        } catch (TrafficGameException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new TrafficGameException("Failed to generate new game round: " + e.getMessage());
//        }
//    }

    public TrafficGameResponse generateNewRound() {
        try {
            List<TrafficEdge> edges = generateRandomEdges();

            // Use the proper benchmark methods
            int ffMaxFlow = fordFulkersonService.computeMaxFlow(edges);
            int ekMaxFlow = edmondsKarpService.computeMaxFlow(edges);

            if (ffMaxFlow != ekMaxFlow) {
                throw TrafficGameException.algorithmFailure();
            }

            long ffTime = fordFulkersonService.measureTime(edges);  // now in nanoseconds
            long ekTime = edmondsKarpService.measureTime(edges);    // now in nanoseconds

            TrafficGameRound round = new TrafficGameRound(ffMaxFlow, ffTime, ekTime);
            round = roundRepository.save(round);

            for (TrafficEdge edge : edges) {
                edge.setRoundId(round.getId());
            }

            Map<String, Integer> capacities = buildCapacityMap(edges);

            return new TrafficGameResponse(
                    round.getId(), edges, capacities, ffMaxFlow, ffTime, ekTime
            );

        } catch (TrafficGameException e) {
            throw e;
        } catch (Exception e) {
            throw new TrafficGameException("Failed to generate new game round: " + e.getMessage());
        }
    }

    public TrafficGameResponse submitAnswer(TrafficAnswerRequest request) {
        validateAnswerRequest(request);

        TrafficGameRound round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> TrafficGameException.roundNotFound(request.getRoundId()));

        int correctMaxFlow = round.getCorrectMaxFlow();
        int playerAnswer = request.getPlayerAnswer();

        boolean isCorrect = playerAnswer == correctMaxFlow;
        String result;

        if (playerAnswer == correctMaxFlow) {
            result = "WIN";
        } else if (Math.abs(playerAnswer - correctMaxFlow) <= 1) {
            result = "DRAW";
        } else {
            result = "LOSE";
        }

        isCorrect = result.equals("WIN");

        TrafficGameResult gameResult = new TrafficGameResult(
                request.getPlayerName(),
                playerAnswer,
                correctMaxFlow,
                isCorrect,
                result,
                round.getId(),
                round.getFordFulkersonTimeMs(),
                round.getEdmondsKarpTimeMs()
        );

        resultRepository.save(gameResult);

        TrafficGameResponse response = new TrafficGameResponse(
                round.getId(), null, null, correctMaxFlow,
                round.getFordFulkersonTimeMs(), round.getEdmondsKarpTimeMs()
        );
        response.setCorrect(isCorrect);
        response.setMessage(buildResultMessage(result, correctMaxFlow, playerAnswer));
        response.setPlayerName(request.getPlayerName());

        return response;
    }

    public List<TrafficGameResult> getCorrectAnswers() {
        return resultRepository.findByIsCorrectTrue();
    }

    public List<TrafficGameResult> getResultsByRound(Long roundId) {
        if (roundId == null || roundId <= 0) {
            throw new TrafficGameException("Invalid round id");
        }
        return resultRepository.findByRoundId(roundId);
    }

    public List<TrafficGameRound> getAllRounds() {
        return roundRepository.findAllByOrderByCreatedAtDesc();
    }

    private List<TrafficEdge> generateRandomEdges() {
        Random random = new Random();
        List<TrafficEdge> edges = new ArrayList<>();

        for (String[] edge : EDGES) {
            int capacity = 5 + random.nextInt(11);
            edges.add(new TrafficEdge(edge[0], edge[1], capacity, null));
        }

        return edges;
    }

    public List<TrafficGameResult> getLeaderboard() {
        return resultRepository.findAllByOrderByIsCorrectDescCreatedAtDesc();
    }

    private Map<String, Integer> buildCapacityMap(List<TrafficEdge> edges) {
        Map<String, Integer> capacities = new LinkedHashMap<>();
        for (TrafficEdge edge : edges) {
            String key = edge.getSource() + "->" + edge.getDestination();
            capacities.put(key, edge.getCapacity());
        }
        return capacities;
    }

    private void validateAnswerRequest(TrafficAnswerRequest request) {
        if (request == null) {
            throw new TrafficGameException("Request cannot be null");
        }

        if (request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
            throw TrafficGameException.invalidPlayerName();
        }

        if (request.getPlayerName().trim().length() < 2) {
            throw new TrafficGameException(
                    "Player name must be at least 2 characters",
                    "INVALID_PLAYER_NAME",
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        if (request.getPlayerAnswer() < 0) {
            throw TrafficGameException.invalidAnswer();
        }

        if (request.getRoundId() == null || request.getRoundId() <= 0) {
            throw TrafficGameException.roundNotFound(request.getRoundId());
        }
    }

    private String buildResultMessage(String result, int correctAnswer, int playerAnswer) {
        switch (result) {
            case "WIN":
                return "Correct! The maximum flow is " + correctAnswer + " vehicles/minute!";
            case "DRAW":
                return "So close! Your answer was " + playerAnswer +
                        " but the correct maximum flow is " + correctAnswer +
                        " vehicles/minute. You were within 1!";
            default:
                return "Wrong! Your answer was " + playerAnswer +
                        " but the correct maximum flow is " + correctAnswer +
                        " vehicles/minute!";
        }
    }
}