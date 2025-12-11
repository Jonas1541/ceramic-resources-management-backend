package com.jonasdurau.ceramicmanagement.bisquefiring.employeeusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BisqueFiringEmployeeUsageRepository extends JpaRepository<BisqueFiringEmployeeUsage, Long>{
    
    boolean existsByEmployeeId(Long employeeId);
}
