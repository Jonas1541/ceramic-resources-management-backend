package com.jonasdurau.ceramicmanagement.config;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MainDatabaseMigrationConfig {

    @Autowired
    @Qualifier("mainActualDataSource")
    private DataSource mainDataSource;

    @PostConstruct
    public void migrateMainDatabase() {
        Flyway flyway = Flyway.configure()
                .dataSource(mainDataSource)
                .locations("classpath:db/migration/main")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();

        flyway.migrate();
        System.out.println("Flyway: Main DB migrado/verificado com sucesso.");
    }
}