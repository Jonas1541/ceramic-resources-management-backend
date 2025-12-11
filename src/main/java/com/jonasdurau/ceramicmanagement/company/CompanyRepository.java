package com.jonasdurau.ceramicmanagement.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmail(String email);

    boolean existsByCnpj(String cnpj);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Company c WHERE (c.lastActivityAt < :cutoffDate) OR (c.lastActivityAt IS NULL AND c.createdAt < :cutoffDate)")
    List<Company> findInactiveCompanies(@Param("cutoffDate") Instant cutoffDate);

    List<Company> findByMarkedForDeletionTrueAndDeletionScheduledAtBefore(Instant date);
}
