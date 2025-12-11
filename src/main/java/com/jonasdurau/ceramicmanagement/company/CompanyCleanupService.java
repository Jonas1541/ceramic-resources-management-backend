package com.jonasdurau.ceramicmanagement.company;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.company.dto.CleanupResultDTO;
import com.jonasdurau.ceramicmanagement.company.exception.CleanupJobException;
import com.jonasdurau.ceramicmanagement.company.exception.PartialCleanupFailureException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyCleanupService.class);
    private final CompanyRepository companyRepository;
    private final DatabaseService databaseService;

    @Autowired
    public CompanyCleanupService(CompanyRepository companyRepository, DatabaseService databaseService) {
        this.companyRepository = companyRepository;
        this.databaseService = databaseService;
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public CleanupResultDTO deleteInactiveCompanies() {
        try {
            logger.info("Iniciando job de limpeza de empresas inativas...");
            Instant cutoffDate = Instant.now().minus(365, ChronoUnit.DAYS);
            List<Company> inactiveCompanies = companyRepository.findInactiveCompanies(cutoffDate);
            
            if (inactiveCompanies.isEmpty()) {
                String msg = "Nenhuma empresa inativa encontrada para exclusão.";
                logger.info(msg);
                return new CleanupResultDTO(0, 0, msg);
            }
            
            logger.info("Encontradas {} empresas inativas para possível exclusão.", inactiveCompanies.size());
            
            List<Company> deletionFailures = new ArrayList<>();
            
            for (Company company : inactiveCompanies) {
                try {
                    databaseService.dropTenantDatabase(company.getDatabaseName());
                    companyRepository.delete(company);
                } catch (Exception e) {
                    logger.error("Falha ao processar exclusão da empresa {}", company.getName(), e);
                    deletionFailures.add(company);
                }
            }
            
            if (!deletionFailures.isEmpty()) {
                int successCount = inactiveCompanies.size() - deletionFailures.size();
                int failureCount = deletionFailures.size();
                String errorMessage = String.format("Falha ao excluir %d empresas.", failureCount);
                throw new PartialCleanupFailureException(successCount, failureCount, errorMessage);
            }
            
            String message = String.format("Job de limpeza concluído. Excluídas: %d", inactiveCompanies.size());
            logger.info(message);
            return new CleanupResultDTO(inactiveCompanies.size(), 0, message);
            
        } catch (Exception e) {
            logger.error("Erro catastrófico durante o job de limpeza de empresas inativas", e);
            throw new CleanupJobException("Falha geral no job de limpeza: " + e.getMessage(), e);
        }
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public CleanupResultDTO deleteScheduledCompanies() {
        try {
            logger.info("Iniciando job de exclusão de contas agendadas...");
            
            Instant now = Instant.now();
            List<Company> companiesToDelete = companyRepository.findByMarkedForDeletionTrueAndDeletionScheduledAtBefore(now);
            
            if (companiesToDelete.isEmpty()) {
                String msg = "Nenhuma conta agendada para exclusão encontrada.";
                logger.info(msg);
                return new CleanupResultDTO(0, 0, msg);
            }
            
            logger.info("Encontradas {} contas agendadas para exclusão.", companiesToDelete.size());
            
            List<Company> deletionFailures = new ArrayList<>();
            
            for (Company company : companiesToDelete) {
                try {
                    databaseService.dropTenantDatabase(company.getDatabaseName());
                    companyRepository.delete(company);
                } catch (Exception e) {
                    logger.error("Falha ao excluir conta {}", company.getEmail(), e);
                    deletionFailures.add(company);
                }
            }
            
            if (!deletionFailures.isEmpty()) {
                int successCount = companiesToDelete.size() - deletionFailures.size();
                int failureCount = deletionFailures.size();
                String errorMessage = String.format("Falha ao excluir %d contas.", failureCount);
                throw new PartialCleanupFailureException(successCount, failureCount, errorMessage);
            }
            
            String message = String.format("Job de exclusão de contas concluído. Excluídas: %d", companiesToDelete.size());
            logger.info(message);
            return new CleanupResultDTO(companiesToDelete.size(), 0, message);
            
        } catch (Exception e) {
            logger.error("Erro catastrófico durante o job de exclusão de contas agendadas", e);
            throw new CleanupJobException("Falha geral no job de exclusão: " + e.getMessage(), e);
        }
    }
}