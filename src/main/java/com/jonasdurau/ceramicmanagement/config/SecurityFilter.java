package com.jonasdurau.ceramicmanagement.config;

import com.jonasdurau.ceramicmanagement.auth.exception.ExpiredTokenException;
import com.jonasdurau.ceramicmanagement.auth.exception.InvalidTokenException;
import com.jonasdurau.ceramicmanagement.company.Company;
import com.jonasdurau.ceramicmanagement.company.CompanyRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dynamicDataSource;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);
        String tenantId = null;
        try {
            if (token != null) {
                String email = tokenService.getEmailFromToken(token);
                Company company = companyRepository.findByEmail(email).orElse(null);
                if (company != null) {
                    tenantId = company.getDatabaseName();
                    TenantContext.setCurrentTenant(tenantId);
                    logger.info("SecurityFilter: Contexto do tenant definido para '{}' no request para '{}'", tenantId, request.getRequestURI());

                    var authentication = new UsernamePasswordAuthenticationToken(company, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredTokenException | InvalidTokenException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token inválido ou expirado\",\"message\":\"" + ex.getMessage() + "\"}");
        } finally {
            if (tenantId != null) {
                TenantContext.clear();
            }
        }
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}