package com.jonasdurau.ceramicmanagement.services;

import com.jonasdurau.ceramicmanagement.config.DynamicDataSource;
import com.jonasdurau.ceramicmanagement.dtos.request.CompanyRequestDTO;
import com.jonasdurau.ceramicmanagement.dtos.response.CompanyResponseDTO;
import com.jonasdurau.ceramicmanagement.dtos.response.DeletionStatusResponseDTO;
import com.jonasdurau.ceramicmanagement.entities.Company;
import com.jonasdurau.ceramicmanagement.repositories.main.CompanyRepository;
import com.jonasdurau.ceramicmanagement.controllers.exceptions.BusinessException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository; // Repositório do main_db

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dynamicDataSourceBean;

    @PersistenceContext(unitName = "main_db")
    private EntityManager mainEntityManager; // EntityManager específico para main_db

    @Value("${tenant.datasource.base-url}")
    private String tenantDbBaseUrl;

    @Value("${tenant.datasource.port}")
    private int tenantDbPort;

    @Value("${tenant.datasource.username}")
    private String tenantDbUsername;

    @Value("${tenant.datasource.password}")
    private String tenantDbPassword;

    private static final int DELETION_GRACE_PERIOD_DAYS = 30;

    @Transactional(transactionManager = "mainTransactionManager")
    public CompanyResponseDTO registerCompany(CompanyRequestDTO dto) throws IOException {
        if (companyRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Este email já está cadastrado.");
        }
        if (companyRepository.existsByCnpj(dto.cnpj())) {
            throw new BusinessException("Este CNPJ já está cadastrado.");
        }

        String databaseName = "company_" + dto.name().toLowerCase().replace(" ", "_");

        if (!(dynamicDataSourceBean instanceof DynamicDataSource)) {
            throw new IllegalStateException("DataSource injetado não é DynamicDataSource!");
        }
        DynamicDataSource actualDynamicDataSource = (DynamicDataSource) dynamicDataSourceBean;
        Map<Object, DataSource> resolvedDataSources = actualDynamicDataSource.getResolvedDataSources();
        if (resolvedDataSources != null && resolvedDataSources.containsKey(databaseName)) {
            throw new BusinessException("Já existe um tenant registrado com o nome " + databaseName);
        }
        
        try {
            databaseService.createDatabase(databaseName);
        } catch (DataAccessException e) {
            throw new BusinessException("Erro ao criar o banco de dados para o tenant '" + databaseName + "': " + e.getMostSpecificCause().getMessage(), e);
        }

        String cleanBaseUrl = tenantDbBaseUrl.endsWith("/") ? tenantDbBaseUrl.substring(0, tenantDbBaseUrl.length() -1) : tenantDbBaseUrl;
        String tenantJdbcUrl = cleanBaseUrl + "/" + databaseName + "?useSSL=false&allowPublicKeyRetrieval=true"; 

        databaseService.runFlywayMigration(databaseName, tenantJdbcUrl, tenantDbUsername, tenantDbPassword);

        databaseService.addTenant(databaseName, tenantJdbcUrl, tenantDbUsername, tenantDbPassword);

        Company company = new Company();
        company.setName(dto.name());
        company.setEmail(dto.email());
        company.setCnpj(dto.cnpj());
        company.setPassword(passwordEncoder.encode(dto.password()));
        company.setDatabaseUrl(tenantJdbcUrl); 
        company.setDatabasePort(tenantDbPort); 
        company.setDatabaseName(databaseName); 

        companyRepository.save(company);
        return entityToResponseDTO(company);
    }

    @Transactional(transactionManager = "mainTransactionManager") // Especifica o transaction manager para main_db
    public void scheduleCurrentCompanyDeletion() {
        Company principal = (Company) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Company managedCompany = companyRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        
        if (managedCompany.isMarkedForDeletion()) {
            throw new BusinessException("A exclusão desta conta já está agendada.");
        }
        
        managedCompany.setMarkedForDeletion(true);
        managedCompany.setDeletionScheduledAt(Instant.now().plus(DELETION_GRACE_PERIOD_DAYS, ChronoUnit.DAYS));
        
        companyRepository.save(managedCompany);
    }

    @Transactional(transactionManager = "mainTransactionManager") // Especifica o transaction manager para main_db
    public void cancelCurrentCompanyDeletion() {
        Company principal = (Company) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Company managedCompany = companyRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        
        if (!managedCompany.isMarkedForDeletion()) {
            throw new BusinessException("A exclusão desta conta não está agendada");
        }

        managedCompany.setMarkedForDeletion(false);
        managedCompany.setDeletionScheduledAt(null);
        
        companyRepository.save(managedCompany);
    }

    @Transactional(transactionManager = "mainTransactionManager", readOnly = true) // Especifica o transaction manager para main_db
    public DeletionStatusResponseDTO getCurrentCompanyDeletionStatus() {
        Company principal = (Company) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Company managedCompany = companyRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("Empresa não encontrada"));
        
        return new DeletionStatusResponseDTO(
            managedCompany.isMarkedForDeletion(),
            managedCompany.getDeletionScheduledAt()
        );
    }

    private CompanyResponseDTO entityToResponseDTO(Company entity) {
        return new CompanyResponseDTO(
            entity.getId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getName(),
            entity.getEmail(),
            entity.getCnpj(),
            null 
        );
    }
}