package com.jonasdurau.ceramicmanagement.bisquefiring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BisqueFiringRequestDTO;
import com.jonasdurau.ceramicmanagement.bisquefiring.dto.BisqueFiringResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.FiringListDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentController;

@RestController
@RequestMapping("/api/kilns/{parentId}/bisque-firings")
public class BisqueFiringController extends DependentController<FiringListDTO, BisqueFiringRequestDTO, BisqueFiringResponseDTO, Long, BisqueFiringService> {
}
