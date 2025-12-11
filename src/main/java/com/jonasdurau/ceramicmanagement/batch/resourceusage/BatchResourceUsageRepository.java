package com.jonasdurau.ceramicmanagement.batch.resourceusage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchResourceUsageRepository extends JpaRepository<BatchResourceUsage, Long> {

    boolean existsByResourceId(Long resourceId);
}
