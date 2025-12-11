package com.jonasdurau.ceramicmanagement.resource;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jonasdurau.ceramicmanagement.resource.enums.ResourceCategory;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    boolean existsByName(String name);

    Optional<Resource> findByCategory(ResourceCategory category);

    boolean existsByCategory(ResourceCategory category);
    
    boolean existsByCategoryAndIdNot(ResourceCategory category, Long id);
}
