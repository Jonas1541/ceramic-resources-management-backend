package com.jonasdurau.ceramicmanagement.auth;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jonasdurau.ceramicmanagement.auth.dto.ForgotPasswordRequestDTO;
import com.jonasdurau.ceramicmanagement.auth.dto.LoginDTO;
import com.jonasdurau.ceramicmanagement.auth.dto.ResetPasswordRequestDTO;
import com.jonasdurau.ceramicmanagement.auth.dto.TokenResponseDTO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        TokenResponseDTO tokenDTO = authService.login(dto);
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        authService.forgotPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        authService.resetPassword(dto);
        return ResponseEntity.noContent().build();
    }
}
