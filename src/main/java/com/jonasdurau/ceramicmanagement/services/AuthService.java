package com.jonasdurau.ceramicmanagement.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jonasdurau.ceramicmanagement.config.TenantContext;
import com.jonasdurau.ceramicmanagement.config.TokenService;
import com.jonasdurau.ceramicmanagement.entities.Company;
import com.jonasdurau.ceramicmanagement.entities.PasswordResetToken;
import com.jonasdurau.ceramicmanagement.repositories.PasswordResetTokenRepository;
import com.jonasdurau.ceramicmanagement.repositories.main.CompanyRepository;
import com.jonasdurau.ceramicmanagement.controllers.exceptions.InvalidCredentialsException;
import com.jonasdurau.ceramicmanagement.controllers.exceptions.ResourceNotFoundException;
import com.jonasdurau.ceramicmanagement.dtos.LoginDTO;
import com.jonasdurau.ceramicmanagement.dtos.request.ForgotPasswordRequestDTO;
import com.jonasdurau.ceramicmanagement.dtos.request.ResetPasswordRequestDTO;
import com.jonasdurau.ceramicmanagement.dtos.response.TokenResponseDTO;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(CompanyRepository companyRepository, PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public TokenResponseDTO login(LoginDTO dto) {
        TenantContext.clear(); 
        Company company = companyRepository.findByEmail(dto.email())
            .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));
        if (!passwordEncoder.matches(dto.password(), company.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
        company.setLastActivityAt(Instant.now());
        companyRepository.save(company);
        TenantContext.setCurrentTenant(company.getDatabaseName());
        String token = tokenService.generateToken(company);
        return new TokenResponseDTO(token);
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        companyRepository.findByEmail(dto.email()).ifPresent(company -> {
            String token = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS);
            PasswordResetToken passwordResetToken = new PasswordResetToken(token, company, expiryDate);
            passwordResetTokenRepository.save(passwordResetToken);
            emailService.sendPasswordResetEmail(company.getEmail(), token);
        });
    }

    @Transactional(transactionManager = "mainTransactionManager")
    public void resetPassword(ResetPasswordRequestDTO dto) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(dto.token())
            .orElseThrow(() -> new ResourceNotFoundException("Token inválido ou expirado."));

        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new ResourceNotFoundException("Token inválido ou expirado.");
        }

        Company company = passwordResetToken.getCompany();
        company.setPassword(passwordEncoder.encode(dto.password()));
        companyRepository.save(company);
        passwordResetTokenRepository.delete(passwordResetToken);
    }
}