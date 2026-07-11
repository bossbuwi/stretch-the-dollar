package com.paradoxdevs.dollar.exception;

public class PasswordException extends ApiException {

    public PasswordException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public PasswordException(ErrorCode errorCode) {
        super(errorCode);
    }
}
