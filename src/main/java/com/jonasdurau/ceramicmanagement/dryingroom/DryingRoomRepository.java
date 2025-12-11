package com.jonasdurau.ceramicmanagement.dryingroom;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DryingRoomRepository extends JpaRepository<DryingRoom, Long>{

    boolean existsByMachinesId(Long machinesId);

    boolean existsByName(String name);
}
