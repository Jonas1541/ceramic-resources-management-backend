package com.jonasdurau.ceramicmanagement.dryingroom;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomListDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomRequestDTO;
import com.jonasdurau.ceramicmanagement.dryingroom.dto.DryingRoomResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/drying-rooms")
public class DryingRoomController extends IndependentController<DryingRoomListDTO, DryingRoomRequestDTO, DryingRoomResponseDTO, Long, DryingRoomService> {

    @GetMapping("/{id}/yearly-report")
    public ResponseEntity<List<YearReportDTO>> yearlyReport(@PathVariable Long id) {
        List<YearReportDTO> report = service.yearlyReport(id);
        return ResponseEntity.ok(report);
    }
}
