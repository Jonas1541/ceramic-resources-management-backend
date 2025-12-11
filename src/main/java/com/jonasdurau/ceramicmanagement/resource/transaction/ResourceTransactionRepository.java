package com.jonasdurau.ceramicmanagement.resource.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonasdurau.ceramicmanagement.resource.Resource;

public interface ResourceTransactionRepository extends JpaRepository<ResourceTransaction, Long> {

    List<ResourceTransaction> findByResource(Resource resource);

    Optional<ResourceTransaction> findByIdAndResource(Long id, Resource resource);

    boolean existsByResourceId(Long resourceId);
}
