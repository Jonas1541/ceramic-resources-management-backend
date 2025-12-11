package com.jonasdurau.ceramicmanagement.shared.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jonasdurau.ceramicmanagement.auth.exception.ExpiredTokenException;
import com.jonasdurau.ceramicmanagement.auth.exception.InvalidCredentialsException;
import com.jonasdurau.ceramicmanagement.auth.exception.InvalidTokenException;
import com.jonasdurau.ceramicmanagement.company.exception.CleanupJobException;
import com.jonasdurau.ceramicmanagement.company.exception.PartialCleanupError;
import com.jonasdurau.ceramicmanagement.company.exception.PartialCleanupFailureException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validações de campos (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Extrai os erros de validação em um Map<field, message>
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        // Montamos uma mensagem concatenada ou algo personalizado.
        // Aqui, só um join simples:
        String combinedErrors = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), "Validation Exception", combinedErrors, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // Exceções de negócio em geral
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        StandardError err = new StandardError(Instant.now(), status.value(), "Business Exception", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // Exceções de credenciais inválidas (login)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<StandardError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED; // 401
        StandardError err = new StandardError(Instant.now(), status.value(), "Invalid Credentials", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(Instant.now(), status.value(), "Resource Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<StandardError> handleExpiredToken(ExpiredTokenException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandardError err = new StandardError(Instant.now(), status.value(), "Token Expired", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandardError> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandardError err = new StandardError(Instant.now(), status.value(), "Invalid Token", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ResourceDeletionException.class)
    public ResponseEntity<StandardError> handleResourceDeletionException(ResourceDeletionException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        StandardError err = new StandardError(Instant.now(), status.value(), "Resource Deletion Conflict", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleEnumDeserializationError(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        if (ex.getLocalizedMessage().contains("ResourceCategory")) {
            // Monta um corpo de resposta explicando
            StandardError err = new StandardError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Enum Deserialization Error", "Valor inválido para o campo `ResourceCategory`", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        // senão, retorna um genérico
        throw ex;
    }

    @ExceptionHandler(PartialCleanupFailureException.class)
    public ResponseEntity<PartialCleanupError> handlePartialFailure(PartialCleanupFailureException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.MULTI_STATUS; // 207
        
        PartialCleanupError err = new PartialCleanupError(
            Instant.now(),
            status.value(),
            "Partial Cleanup Failure",
            ex.getMessage(),
            request.getRequestURI(),
            ex.getSuccessCount(),
            ex.getFailureCount()
        );
        
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(CleanupJobException.class)
    public ResponseEntity<StandardError> handleCleanupJobException(CleanupJobException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(
            Instant.now(),
            status.value(),
            "Cleanup Job Failure",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }
}
