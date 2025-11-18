package com.jonasdurau.ceramicmanagement.services;

import com.jonasdurau.ceramicmanagement.config.DynamicDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway; // <--- Importante
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    private final JdbcTemplate mainJdbcTemplate;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dynamicDataSourceBean;

    @Value("${tenant.datasource.base-url}")
    private String tenantDbBaseUrl;

    @Value("${tenant.datasource.username}")
    private String tenantDbUsername;

    @Value("${tenant.datasource.password}")
    private String tenantDbPassword;

    @Autowired
    public DatabaseService(@Qualifier("mainActualDataSource") DataSource mainDS) {
        this.mainJdbcTemplate = new JdbcTemplate(mainDS);
    }

    public void createDatabase(String databaseName) {
        String createDatabaseSql = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "`";
        mainJdbcTemplate.execute(createDatabaseSql);
    }

    // --- NOVO MÉTODO: Substitui o antigo initializeSchema ---
    public void runFlywayMigration(String dbName, String jdbcUrl, String username, String password) {
        logger.info("Iniciando migração Flyway para o tenant: {}", dbName);
        
        // Configura o Flyway para este banco específico
        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration/tenants") // Aponta para a pasta criada
                .baselineOnMigrate(true) // CRUCIAL: Se o banco já tem tabelas, marca como V1 e não faz nada
                .baselineVersion("1")    // Define que o estado atual é a versão 1
                .load();

        try {
            flyway.migrate();
            logger.info("Migração Flyway concluída com sucesso para o tenant: {}", dbName);
        } catch (Exception e) {
            logger.error("Erro crítico ao executar migração Flyway para o tenant: {}", dbName, e);
            throw e; // Relança para interromper o fluxo se der erro
        }
    }

    public void addTenant(String tenantId, String jdbcUrl, String username, String password) {
        if (!(dynamicDataSourceBean instanceof DynamicDataSource)) {
            throw new IllegalStateException("DataSource configurado não é DynamicDataSource");
        }
        DynamicDataSource dynamicDataSource = (DynamicDataSource) dynamicDataSourceBean;
        DataSource newTenantDataSource = createHikariDataSource(jdbcUrl, username, password);
        dynamicDataSource.addDataSource(tenantId, newTenantDataSource);
    }

    public void dropTenantDatabase(String databaseName) {
        logger.warn("Iniciando processo de exclusão para o banco de dados do tenant: {}", databaseName);
        try {
            if (dynamicDataSourceBean instanceof DynamicDataSource) {
                ((DynamicDataSource) dynamicDataSourceBean).removeDataSource(databaseName);
            }
            mainJdbcTemplate.execute("DROP DATABASE IF EXISTS `" + databaseName + "`");
            logger.info("Banco de dados do tenant {} dropado com sucesso.", databaseName);
        } catch (Exception e) {
            logger.error("Falha ao dropar o banco de dados {}: {}", databaseName, e.getMessage(), e);
            throw new RuntimeException("Falha ao dropar o banco de dados " + databaseName, e);
        }
    }

    private DataSource createHikariDataSource(String jdbcUrl, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5); // Boa prática para tenants: limitar conexões
        return new HikariDataSource(config);
    }
}