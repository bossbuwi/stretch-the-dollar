package com.paradoxdevs.dollar.error.exception;

import com.paradoxdevs.dollar.error.ErrorCode;

public class PasswordException extends ApiException {

    public PasswordException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public PasswordException(ErrorCode errorCode) {
        super(errorCode);
    }
}
