package com.jonasdurau.ceramicmanagement.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    String email,
    @NotBlank(message = "O CNPJ é obrigatório")
    String cnpj,
    @NotBlank(message = "A senha é obrigatória")
    String password
) {}
