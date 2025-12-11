package com.jonasdurau.ceramicmanagement.company;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jonasdurau.ceramicmanagement.company.dto.CompanyRequestDTO;
import com.jonasdurau.ceramicmanagement.company.dto.CompanyResponseDTO;
import com.jonasdurau.ceramicmanagement.company.dto.DeletionStatusResponseDTO;

import java.io.IOException;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyResponseDTO> registerCompany(@Valid @RequestBody CompanyRequestDTO dto) throws IOException {
        CompanyResponseDTO company = companyService.registerCompany(dto);
        return ResponseEntity.ok(company);
    }

    @PatchMapping("/me/schedule-deletion")
    public ResponseEntity<Void> scheduleOwnAccountDeletion() {
        companyService.scheduleCurrentCompanyDeletion();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/cancel-deletion")
    public ResponseEntity<Void> cancelOwnAccountDeletion() {
        companyService.cancelCurrentCompanyDeletion();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/deletion-status")
    public ResponseEntity<DeletionStatusResponseDTO> getOwnDeletionStatus() {
        return ResponseEntity.ok(companyService.getCurrentCompanyDeletionStatus());
    }
}
