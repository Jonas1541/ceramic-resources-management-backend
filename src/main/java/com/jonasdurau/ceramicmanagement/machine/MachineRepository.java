package com.jonasdurau.ceramicmanagement.machine;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<Machine, Long>{

    boolean existsByName(String name);
}
