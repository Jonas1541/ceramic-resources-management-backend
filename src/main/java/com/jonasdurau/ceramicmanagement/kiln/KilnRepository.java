package com.jonasdurau.ceramicmanagement.kiln;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KilnRepository extends JpaRepository<Kiln, Long>{

    boolean existsByMachinesId(Long machineId);
}
