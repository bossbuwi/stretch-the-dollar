package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.FeatureDisabledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class FeatureDisabledExceptionHandler extends BaseExceptionHandler<FeatureDisabledException> {

    public FeatureDisabledExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return builder.build(request, ErrorCode.FEATURE_DISABLED, null, null, null);
    }
}
