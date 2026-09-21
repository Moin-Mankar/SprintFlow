package com.sprintflow.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = "Invalid value for parameter: " + ex.getName();

        return buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                "Endpoint not found",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "Access denied",
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNAUTHORIZED,
                "Authentication required",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
