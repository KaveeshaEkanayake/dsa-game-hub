package com.dsagamehub.repository;

import com.dsagamehub.model.AlgorithmRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlgorithmRunRepository extends JpaRepository<AlgorithmRun, Long> {
    List<AlgorithmRun> findByGameNameOrderByCreatedAtDesc(String gameName);
}