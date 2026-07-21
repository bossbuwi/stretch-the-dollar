package com.paradoxdevs.dollar.error.exception;

import com.paradoxdevs.dollar.error.ErrorCode;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(final String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
