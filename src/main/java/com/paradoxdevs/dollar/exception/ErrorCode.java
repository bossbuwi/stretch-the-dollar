package com.paradoxdevs.dollar.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    REQUEST_VALIDATION_ERROR(100, "Request validation failed."),
    RESOURCE_NOT_FOUND(101, "Resource not found."),
    MALFORMED_REQUEST(102, "Malformed JSON request, please check your syntax."),
    UNAUTHENTICATED(103, "Please login to access the resource."),
    UNAUTHORIZED(104, "Invalid credentials."),
    EXPIRED_TOKEN(105, "Auth token already expired."),
    INVALID_TOKEN_SIGNATURE(106, "Auth token has invalid signature."),
    MALFORMED_JWT(107, "Malformed JWT, please check your headers."),
    RESOURCE_ALREADY_EXISTS(108, "The %s: %s already exists."),
    INVALID_USER(109, "User is not allowed to perform this operation."),
    INTERNAL_SERVER_ERROR(999, "Internal server error."),;

    private final int code;
    private final String errorMessage;

    ErrorCode(final int code, final String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public String formatMessage(Object... args) {
        if (args == null || args.length == 0) {
            return errorMessage;
        }
        return String.format(errorMessage, args);
    }
}
