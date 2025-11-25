package com.jonasdurau.ceramicmanagement.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.jonasdurau.ceramicmanagement.entities.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByLineId(Long productLineId);

    boolean existsByTypeId(Long productTypeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(Long id);
}
