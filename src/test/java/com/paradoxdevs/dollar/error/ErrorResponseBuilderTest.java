package com.paradoxdevs.dollar.error;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ErrorResponseBuilderTest {

    @Test
    void build_usesCustomMessage_whenProvided_andSetsStatus() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/test/path");

        ResponseEntity<ErrorResponse> resp = builder.build(req, ErrorCode.RESOURCE_NOT_FOUND, "Custom msg", null, null);

        assertEquals(404, resp.getStatusCode().value());
        ErrorResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), body.getCode());
        assertEquals("Custom msg", body.getMessage());
        assertEquals("/test/path", body.getPath());
        assertNotNull(body.getTimestamp());
        assertNull(body.getFieldErrors());
    }

    @Test
    void build_formatsMessageWithArgs_andFiltersFieldErrors() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/xyz");

        List<String> fieldErrors = java.util.Arrays.asList(null, "", "err1", "err2");
        ResponseEntity<ErrorResponse> resp = builder.build(req, ErrorCode.RESOURCE_ALREADY_EXISTS, null, fieldErrors, new Object[]{"User","bob"});

        assertEquals(422, resp.getStatusCode().value());
        ErrorResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), body.getCode());
        assertEquals("The User: bob already exists.", body.getMessage());
        assertEquals("/api/xyz", body.getPath());
        assertNotNull(body.getTimestamp());
        assertNotNull(body.getFieldErrors());
        assertEquals(2, body.getFieldErrors().size());
        assertEquals("err1", body.getFieldErrors().get(0));
    }

    @Test
    void build_returnsInternalServerForUnknownCode_default() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/x");

        ResponseEntity<ErrorResponse> resp = builder.build(req, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
        assertEquals(500, resp.getStatusCode().value());
    }

    @Test
    void build_status_mappings_cover_all_cases() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/p");

        java.util.Map<ErrorCode, Integer> expected = new java.util.HashMap<>();
        expected.put(ErrorCode.UNAUTHENTICATED, 401);
        expected.put(ErrorCode.MALFORMED_JWT, 401);
        expected.put(ErrorCode.UNAUTHORIZED, 403);
        expected.put(ErrorCode.EXPIRED_TOKEN, 403);
        expected.put(ErrorCode.INVALID_TOKEN_SIGNATURE, 403);
        expected.put(ErrorCode.INVALID_USER, 403);
        expected.put(ErrorCode.REQUEST_VALIDATION_ERROR, 400);
        expected.put(ErrorCode.MALFORMED_REQUEST, 400);
        expected.put(ErrorCode.RESOURCE_NOT_FOUND, 404);
        expected.put(ErrorCode.RESOURCE_ALREADY_EXISTS, 422);
        expected.put(ErrorCode.FEATURE_DISABLED, 503);
        expected.put(ErrorCode.INTERNAL_SERVER_ERROR, 500);

        for (java.util.Map.Entry<ErrorCode, Integer> e : expected.entrySet()) {
            ResponseEntity<ErrorResponse> resp = builder.build(req, e.getKey(), null, null, null);
            assertEquals(e.getValue().intValue(), resp.getStatusCode().value(), "Mismatch for " + e.getKey());
        }
    }

    @Test
    void build_usesFormatMessage_whenCustomBlank_andHandlesNullArgs() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/blank");

        // blank custom message should fallback to formatMessage
        ResponseEntity<ErrorResponse> resp = builder.build(req, ErrorCode.PASSWORDS_DONT_MATCH, "   ", null, null);
        assertEquals(500, resp.getStatusCode().value());
        // message should come from ErrorCode
        ErrorResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(ErrorCode.PASSWORDS_DONT_MATCH.getCode(), body.getCode());
        assertEquals(ErrorCode.PASSWORDS_DONT_MATCH.formatMessage(), body.getMessage());
    }

    @Test
    void build_fieldErrors_blankOnly_becomesNull() {
        ErrorResponseBuilder builder = new ErrorResponseBuilder();
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/fields");

        java.util.List<String> fieldErrors = java.util.Arrays.asList("", null, "   ");
        ResponseEntity<ErrorResponse> resp = builder.build(req, ErrorCode.RESOURCE_NOT_FOUND, null, fieldErrors, null);
        ErrorResponse body = resp.getBody();
        assertNotNull(body);
        assertNull(body.getFieldErrors());
    }
}
