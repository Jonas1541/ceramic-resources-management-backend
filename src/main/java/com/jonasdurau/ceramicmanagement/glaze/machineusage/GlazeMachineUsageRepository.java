package com.jonasdurau.ceramicmanagement.glaze.machineusage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlazeMachineUsageRepository extends JpaRepository<GlazeMachineUsage, Long> {

    boolean existsByMachineId(Long machineId);

    List<GlazeMachineUsage> findByMachineId(Long machineId);
}
