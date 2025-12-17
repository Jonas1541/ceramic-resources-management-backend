package com.jonasdurau.ceramicmanagement.batch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.batch.dto.BatchListDTO;
import com.jonasdurau.ceramicmanagement.batch.dto.BatchRequestDTO;
import com.jonasdurau.ceramicmanagement.batch.dto.BatchResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.dto.YearReportDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/batches")
public class BatchController extends IndependentController<BatchListDTO, BatchRequestDTO, BatchResponseDTO, Long, BatchService>{

    @Autowired
    public BatchController(BatchService service) {
        super(service);
    }

    @GetMapping("/yearly-report")
    public ResponseEntity<List<YearReportDTO>> yearlyReport() {
        List<YearReportDTO> report = service.yearlyReport();
        return ResponseEntity.ok(report);
    }
}
