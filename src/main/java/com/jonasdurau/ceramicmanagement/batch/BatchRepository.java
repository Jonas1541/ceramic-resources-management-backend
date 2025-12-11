package com.jonasdurau.ceramicmanagement.batch;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    @Query("SELECT COALESCE(SUM(b.batchFinalCostAtTime), 0) FROM Batch b")
    BigDecimal getTotalFinalCost();

    @Query("SELECT COALESCE(SUM(b.weight), 0) FROM Batch b")
    Double getTotalWeight();

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Batch b")
    boolean anyExists();
}
