package com.jonasdurau.ceramicmanagement.dryingroom.dryingsession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.dto.DryingSessionRequestDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dryingsession.dto.DryingSessionResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentController;

@RestController
@RequestMapping("/api/drying-rooms/{parentId}/drying-sessions")
public class DryingSessionController extends DependentController<DryingSessionResponseDTO, DryingSessionRequestDTO, DryingSessionResponseDTO, Long, DryingSessionService> {

    @Autowired
    public DryingSessionController(DryingSessionService service) {
        super(service);
    }
}
