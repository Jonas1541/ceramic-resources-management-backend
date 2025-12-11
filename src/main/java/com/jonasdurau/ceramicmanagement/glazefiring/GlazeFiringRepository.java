package com.jonasdurau.ceramicmanagement.glazefiring;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlazeFiringRepository extends JpaRepository<GlazeFiring, Long>{

    boolean existsByKilnId(Long kilnId);

    List<GlazeFiring> findByKilnId(Long KilnId);

    Optional<GlazeFiring> findByIdAndKilnId(Long id, Long kilnId);
}
