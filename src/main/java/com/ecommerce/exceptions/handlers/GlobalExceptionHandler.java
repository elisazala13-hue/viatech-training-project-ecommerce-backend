package com.ecommerce.exceptions.handlers;

import com.ecommerce.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        return build(ErrorCode.VALIDATION_FAILED, "Validation failed", req, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpServletRequest req) {
        return build(ErrorCode.BAD_REQUEST, "Malformed JSON request", req);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex,
                                                           HttpServletRequest req) {
        return build(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getMessage(), req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(HttpServletRequest req) {
        return build(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(HttpServletRequest req) {
        return build(ErrorCode.ACCOUNT_SUSPENDED, "Account is suspended", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(HttpServletRequest req) {
        return build(ErrorCode.ACCESS_DENIED, "You do not have permission", req);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest req) {
        return build(ErrorCode.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
                                                        HttpServletRequest req) {
        return build(ErrorCode.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(HttpServletRequest req) {
        return build(ErrorCode.BAD_REQUEST, "Invalid request parameter", req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
                                                          HttpServletRequest req) {
        return build(ErrorCode.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(HttpServletRequest req) {
        return build(ErrorCode.PAYLOAD_TOO_LARGE, "File is too large", req);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefresh(HttpServletRequest req) {
        return build(ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(HttpServletRequest req) {
        return build(ErrorCode.INTERNAL_ERROR, "Something went wrong", req);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePayment(PaymentException ex, HttpServletRequest req) {
        return build(ErrorCode.PAYMENT_PROVIDER_ERROR, ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, String message,
                                                HttpServletRequest req) {
        return build(code, message, req, null);
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, String message,
                                                HttpServletRequest req,
                                                Map<String, String> fieldErrors) {
        return ResponseEntity.status(code.getHttpStatus()).body(new ErrorResponse(
                code.name(),
                code.getStatusValue(),
                code.getHttpStatus().getReasonPhrase(),
                message,
                req.getRequestURI(),
                Instant.now(),
                fieldErrors
        ));
    }
}