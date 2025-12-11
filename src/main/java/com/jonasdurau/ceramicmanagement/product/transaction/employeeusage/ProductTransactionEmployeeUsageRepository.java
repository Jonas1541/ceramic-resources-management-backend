package com.jonasdurau.ceramicmanagement.product.transaction.employeeusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTransactionEmployeeUsageRepository extends JpaRepository<ProductTransactionEmployeeUsage, Long> {
    
    boolean existsByEmployeeId(Long employeeId);
}
