package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
public class HttpMessageNotReadableExceptionHandler extends BaseExceptionHandler<HttpMessageNotReadableException> {

    public HttpMessageNotReadableExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        logMalformedRequest(ex, request);
        return builder.build(request, ErrorCode.MALFORMED_JSON_REQUEST, null, null, null);
    }

    private void logMalformedRequest(Exception ex, WebRequest request) {
        if (!(request instanceof ServletWebRequest servletWebRequest)) return;

        Principal principal = servletWebRequest.getRequest().getUserPrincipal();
        if (!(principal instanceof Authentication auth)) return;

        log.warn("Malformed JSON from user [{}]: {}", resolveUser(auth), ex.getMessage());
    }

    private String resolveUser(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof Map<?, ?> map && map.get("uuid") != null) {
            return map.get("uuid").toString();
        }
        return auth.getName();
    }
}
