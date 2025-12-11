package com.jonasdurau.ceramicmanagement.product.type;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    boolean existsByName(String name);
}
