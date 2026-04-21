package com.dsagamehub.repository;

import com.dsagamehub.model.TrafficGameRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrafficGameRoundRepository extends JpaRepository<TrafficGameRound, Long> {

    List<TrafficGameRound> findAllByOrderByCreatedAtDesc();

}