package com.paradoxdevs.dollar.filter;

import com.paradoxdevs.dollar.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.paradoxdevs.dollar.helper.JwtDataHelper.CORRUPTED_TOKEN;
import static com.paradoxdevs.dollar.helper.JwtDataHelper.INVALID_TOKEN;
import static com.paradoxdevs.dollar.helper.JwtDataHelper.USERNAME;
import static com.paradoxdevs.dollar.helper.JwtDataHelper.VALID_TOKEN;
import static com.paradoxdevs.dollar.helper.JwtDataHelper.createRoles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private JwtService jwtService;
    @Mock
    private HandlerExceptionResolver resolver;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // Clear the security context before each test to guarantee an unauthenticated baseline
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Clean up thread-local storage after each test run
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should skip filtering when Authorization header is missing")
    void shouldSkipFilteringWhenAuthHeaderIsMissing() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should skip filtering when Authorization header does not start with Bearer")
    void shouldSkipFilteringWhenAuthHeaderIsNotBearer() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + VALID_TOKEN);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should authenticate user successfully with valid JWT")
    void shouldAuthenticateUserWithValidJwt() throws ServletException, IOException {
        String uuid = UUID.randomUUID().toString();
        List<String> roles = createRoles();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN);

        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(USERNAME);
        when(jwtService.extractUuid(VALID_TOKEN)).thenReturn(uuid);
        when(jwtService.extractRoles(VALID_TOKEN)).thenReturn(roles);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + INVALID_TOKEN);

        when(jwtService.isTokenValid(INVALID_TOKEN)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should forward exceptions to HandlerExceptionResolver when extraction fails")
    void shouldForwardExceptionsToResolver() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + CORRUPTED_TOKEN);

        RuntimeException jwtException = new RuntimeException("Signature verification failed");
        when(jwtService.isTokenValid(CORRUPTED_TOKEN)).thenThrow(jwtException);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Confirm that the filter caught the error and routed it safely to your exception gateway
        verify(resolver, times(1)).resolveException(request, response, null, jwtException);
        // Ensure execution broke out early and did NOT try to advance the normal filter chain
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
