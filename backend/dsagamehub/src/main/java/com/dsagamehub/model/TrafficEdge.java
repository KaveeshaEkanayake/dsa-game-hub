package com.dsagamehub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "traffic_edges")
public class TrafficEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source")
    private String source;

    @Column(name = "destination")
    private String destination;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "round_id")
    private Long roundId;

    public TrafficEdge() {}

    public TrafficEdge(String source, String destination, int capacity, Long roundId) {
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
        this.roundId = roundId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }
}