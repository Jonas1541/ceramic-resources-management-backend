package com.jonasdurau.ceramicmanagement.company.dto;

import java.time.Instant;

public record CompanyResponseDTO(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String name,
    String email,
    String cnpj,
    String password
) {}
