package com.jonasdurau.ceramicmanagement.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.jonasdurau.ceramicmanagement.auth.exception.ExpiredTokenException;
import com.jonasdurau.ceramicmanagement.auth.exception.InvalidTokenException;
import com.jonasdurau.ceramicmanagement.company.Company;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final long EXPIRATION_TIME_HOURS = 24; 

    public String generateToken(Company company) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Instant now = Instant.now();
            Instant expirationTime = now.plus(EXPIRATION_TIME_HOURS, ChronoUnit.HOURS);

            return JWT.create()
                    .withIssuer("ceramic-management-api")
                    .withSubject(company.getEmail()) 
                    .withClaim("companyId", company.getId())
                    .withClaim("tenantId", company.getDatabaseName()) 
                    .withIssuedAt(now)
                    .withExpiresAt(expirationTime)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("ceramic-management-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (com.auth0.jwt.exceptions.TokenExpiredException exception) {
            throw new ExpiredTokenException("Token JWT expirou.");
        } catch (JWTVerificationException exception) {
            throw new InvalidTokenException("Token JWT inválido ou não pôde ser verificado.");
        }
    }

    public String getTenantIdFromToken(String token) {
         try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("ceramic-management-api")
                    .build()
                    .verify(token)
                    .getClaim("tenantId").asString();
        } catch (com.auth0.jwt.exceptions.TokenExpiredException exception) {
            throw new ExpiredTokenException("Token JWT expirou.");
        } catch (JWTVerificationException exception) {
            throw new InvalidTokenException("Token JWT inválido ou não pôde ser verificado.");
        }
    }
}