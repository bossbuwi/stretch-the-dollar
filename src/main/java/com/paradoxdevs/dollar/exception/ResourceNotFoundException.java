package com.paradoxdevs.dollar.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(final String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
