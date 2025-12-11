package com.jonasdurau.ceramicmanagement.batch.machineusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchMachineUsageRepository extends JpaRepository<BatchMachineUsage, Long> {

    boolean existsByMachineId(Long machineId);
}
