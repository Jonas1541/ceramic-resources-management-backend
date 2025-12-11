package com.jonasdurau.ceramicmanagement.glaze.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlazeTransactionRepository extends JpaRepository<GlazeTransaction, Long> {

    boolean existsByGlazeId(Long glazeId);
}
