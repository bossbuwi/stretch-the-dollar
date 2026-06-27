package com.paradoxdevs.dollar.exception;

public class FeatureDisabledException extends ApiException {

    public FeatureDisabledException() {
        super(ErrorCode.FEATURE_DISABLED);
    }
}
