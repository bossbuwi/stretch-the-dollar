package com.paradoxdevs.dollar.error.exception;

import com.paradoxdevs.dollar.error.ErrorCode;

public class FeatureDisabledException extends ApiException {

    public FeatureDisabledException() {
        super(ErrorCode.FEATURE_DISABLED);
    }
}
