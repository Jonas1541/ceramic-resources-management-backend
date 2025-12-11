package com.jonasdurau.ceramicmanagement.glaze.resourceusage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlazeResourceUsageRepository extends JpaRepository<GlazeResourceUsage, Long> {

    boolean existsByResourceId(Long resourceId);

    List<GlazeResourceUsage> findByResourceId(Long resourceId);
}
