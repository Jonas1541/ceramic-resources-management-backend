package com.jonasdurau.ceramicmanagement.config;

import com.jonasdurau.ceramicmanagement.company.Company;
import com.jonasdurau.ceramicmanagement.company.CompanyRepository;
import com.jonasdurau.ceramicmanagement.company.DatabaseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TenantInitializer.class);
    private final CompanyRepository companyRepository;
    private final DatabaseService databaseService;
    private final String tenantDbUsername;
    private final String tenantDbPassword;

    @Autowired
    public TenantInitializer(CompanyRepository companyRepository, DatabaseService databaseService,
            @Value("${tenant.datasource.username}") String tenantDbUsername,
            @Value("${tenant.datasource.password}") String tenantDbPassword) {
        this.companyRepository = companyRepository;
        this.databaseService = databaseService;
        this.tenantDbUsername = tenantDbUsername;
        this.tenantDbPassword = tenantDbPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Iniciando inicialização dos tenants...");
        TenantContext.clear(); 

        List<Company> companies = companyRepository.findAll();
        
        for (Company company : companies) {
            String tenantId = company.getDatabaseName(); 
            String jdbcUrl = company.getDatabaseUrl();   
            
            try {
                // 1. Roda o Flyway (Cria tabelas OU Apenas faz Baseline se já existirem)
                databaseService.runFlywayMigration(tenantId, jdbcUrl, tenantDbUsername, tenantDbPassword);

                // 2. Adiciona ao pool de conexões
                databaseService.addTenant(tenantId, jdbcUrl, tenantDbUsername, tenantDbPassword);
                
                logger.info("Tenant {} inicializado com sucesso.", tenantId);
            } catch (Exception e) {
                logger.error("FALHA ao inicializar tenant {}. O sistema continuará para os próximos.", tenantId, e);
                // Não lançamos exceção aqui para que um tenant quebrado não derrube o sistema todo
            }
        }
        logger.info("Processo de inicialização dos tenants finalizado.");
    }
}