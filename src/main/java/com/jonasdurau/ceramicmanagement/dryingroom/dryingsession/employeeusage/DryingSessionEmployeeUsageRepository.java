package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.employeeusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DryingSessionEmployeeUsageRepository extends JpaRepository<DryingSessionEmployeeUsage, Long> {
    boolean existsByEmployeeId(Long employeeId);
}
