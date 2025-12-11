package com.jonasdurau.ceramicmanagement.auth;

import java.time.Instant;

import com.jonasdurau.ceramicmanagement.company.Company;
import com.jonasdurau.ceramicmanagement.shared.persistence.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_password_reset_token")
public class PasswordResetToken extends BaseEntity {

    private String token;

    @OneToOne(targetEntity = Company.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "company_id")
    private Company company;

    private Instant expiryDate;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, Company company, Instant expiryDate) {
        this.token = token;
        this.company = company;
        this.expiryDate = expiryDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
}
