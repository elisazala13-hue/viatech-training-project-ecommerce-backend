package com.ecommerce.exceptions.handlers;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
    CONFLICT(HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE),
    PAYMENT_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) { this.httpStatus = httpStatus; }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public int getStatusValue() { return httpStatus.value(); }
}
