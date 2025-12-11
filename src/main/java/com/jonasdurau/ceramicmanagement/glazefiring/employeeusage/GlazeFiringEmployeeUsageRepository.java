package com.jonasdurau.ceramicmanagement.glazefiring.employeeusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlazeFiringEmployeeUsageRepository extends JpaRepository<GlazeFiringEmployeeUsage, Long>{
    
    boolean existsByEmployeeId(Long employeeId);
}
