package com.jonasdurau.ceramicmanagement.company;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.company.dto.CleanupResultDTO;

@RestController
@RequestMapping("/api/internal/tasks")
public class InternalJobController {

    private static final Logger logger = LoggerFactory.getLogger(InternalJobController.class);

    @Autowired
    private CompanyCleanupService companyCleanupService;

    @PostMapping("/trigger-company-cleanup")
    public ResponseEntity<CleanupResultDTO> triggerCompanyCleanup() {
        logger.info("Recebida solicitação para executar o job de limpeza de empresas.");
        CleanupResultDTO result = companyCleanupService.deleteInactiveCompanies();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/trigger-account-deletion")
    public ResponseEntity<CleanupResultDTO> triggerAccountDeletionJob() {
        logger.info("Recebida solicitação para executar o job de exclusão de contas agendadas.");
        CleanupResultDTO result = companyCleanupService.deleteScheduledCompanies();
        return ResponseEntity.ok(result);
    }
}