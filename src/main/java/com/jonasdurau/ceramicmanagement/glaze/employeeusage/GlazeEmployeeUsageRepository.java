package com.jonasdurau.ceramicmanagement.glaze.employeeusage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlazeEmployeeUsageRepository extends JpaRepository<GlazeEmployeeUsage, Long>{

    boolean existsByEmployeeId(Long employeeId);

    List<GlazeEmployeeUsage> findByEmployeeId(Long employeeId);
}
