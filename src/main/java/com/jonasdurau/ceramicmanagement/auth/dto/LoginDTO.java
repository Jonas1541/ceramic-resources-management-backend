package com.jonasdurau.ceramicmanagement.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
    @NotBlank(message = "O email não pode estar vazio.")
    @Email(message = "Por favor, insira um email válido.")
    String email,
    @NotBlank(message = "A senha não pode estar vazia.")
    String password
) {}
