package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DryingSessionRepository extends JpaRepository<DryingSession, Long>{

    boolean existsByDryingRoomId(Long dryingRoomId);
}
