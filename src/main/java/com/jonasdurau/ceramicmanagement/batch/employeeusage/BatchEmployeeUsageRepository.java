package com.jonasdurau.ceramicmanagement.batch.employeeusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchEmployeeUsageRepository extends JpaRepository<BatchEmployeeUsage, Long>{

    boolean existsByEmployeeId(Long employeeId);
}
