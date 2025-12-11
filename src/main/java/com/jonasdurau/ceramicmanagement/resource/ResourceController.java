package com.jonasdurau.ceramicmanagement.resource;

import com.jonasdurau.ceramicmanagement.resource.dto.ResourceListDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceRequestDTO;
import com.jonasdurau.ceramicmanagement.resource.dto.ResourceResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController extends IndependentController<ResourceListDTO, ResourceRequestDTO, ResourceResponseDTO, Long, ResourceService> {

    @GetMapping("/{id}/yearly-report")
    public ResponseEntity<List<YearReportDTO>> yearlyReport(@PathVariable Long id) {
        List<YearReportDTO> report = service.yearlyReport(id);
        return ResponseEntity.ok(report);
    }
}
