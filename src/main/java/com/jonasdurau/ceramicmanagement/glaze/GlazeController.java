package com.jonasdurau.ceramicmanagement.glaze;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeListDTO;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeRequestDTO;
import com.jonasdurau.ceramicmanagement.glaze.dto.GlazeResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/glazes")
public class GlazeController extends IndependentController<GlazeListDTO, GlazeRequestDTO, GlazeResponseDTO, Long, GlazeService> {

    @GetMapping("/{id}/yearly-report")
    public ResponseEntity<List<YearReportDTO>> yearlyReport(@PathVariable Long id) {
        List<YearReportDTO> report = service.yearlyReport(id);
        return ResponseEntity.ok(report);
    }
}
