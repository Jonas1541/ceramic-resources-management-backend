package com.jonasdurau.ceramicmanagement.glazefiring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlazeFiringRequestDTO;
import com.jonasdurau.ceramicmanagement.glazefiring.dto.GlazeFiringResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.FiringListDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.DependentController;

@RestController
@RequestMapping("/api/kilns/{parentId}/glaze-firings")
public class GlazeFiringController extends DependentController<FiringListDTO, GlazeFiringRequestDTO, GlazeFiringResponseDTO, Long, GlazeFiringService> {
}
