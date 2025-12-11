package com.jonasdurau.ceramicmanagement.product.line;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLineRepository extends JpaRepository<ProductLine, Long> {

    boolean existsByName(String name);
}
