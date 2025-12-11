package com.jonasdurau.ceramicmanagement.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDTO(
    @NotBlank String token,
    @NotBlank String password
) {
}
