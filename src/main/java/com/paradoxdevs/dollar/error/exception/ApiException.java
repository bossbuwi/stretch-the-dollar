package com.paradoxdevs.dollar.error.exception;

import com.paradoxdevs.dollar.error.ErrorCode;
import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
