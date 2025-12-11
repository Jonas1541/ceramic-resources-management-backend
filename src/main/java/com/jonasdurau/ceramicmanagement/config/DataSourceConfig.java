package com.jonasdurau.ceramicmanagement.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = {"com.jonasdurau.ceramicmanagement.company", "com.jonasdurau.ceramicmanagement.auth"},
    entityManagerFactoryRef = "mainEntityManagerFactory",
    transactionManagerRef = "mainTransactionManager"
)
public class DataSourceConfig {

    @Value("${main.datasource.url}")
    private String mainDbUrl;

    @Value("${main.datasource.username}")
    private String mainDbUsername;

    @Value("${main.datasource.password}")
    private String mainDbPassword;

    @Bean("mainActualDataSource")
    public DataSource mainActualDataSource() {
        return createDataSource(mainDbUrl, mainDbUsername, mainDbPassword);
    }

    @Bean("dataSource")
    @Primary
    public DataSource dynamicDataSource(@Qualifier("mainActualDataSource") DataSource actualMainDataSource) {
        return new DynamicDataSource(actualMainDataSource);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            @Qualifier("mainActualDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("com.jonasdurau.ceramicmanagement.company", "com.jonasdurau.ceramicmanagement.auth")
                .persistenceUnit("main_db")
                .build();
    }

    @Bean
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") EntityManagerFactory mainEntityManagerFactory) {
        return new JpaTransactionManager(mainEntityManagerFactory);
    }

    private DataSource createDataSource(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDataSource(config);
    }
} 