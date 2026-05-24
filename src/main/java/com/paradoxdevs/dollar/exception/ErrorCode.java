package com.paradoxdevs.dollar.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    REQUEST_VALIDATION_ERROR(100, "Request validation failed."),
    RESOURCE_NOT_FOUND(101, "Resource not found."),
    MALFORMED_REQUEST(102, "Malformed JSON request, please check your syntax."),
    INTERNAL_SERVER_ERROR(999, "Internal server error."),;

    private final int code;
    private final String errorMessage;

    ErrorCode(final int code, final String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
