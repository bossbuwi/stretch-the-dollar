package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.service.impl.JwtServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @Mock
    private UserDetails userDetails;
    @Spy
    private Clock clock = Clock.systemDefaultZone();
    @InjectMocks
    private JwtServiceImpl jwtService;
    private final String username = "paradoxdevs";

    @BeforeEach
    void setup() {
        when(userDetails.getUsername()).thenReturn(username);
    }

    @Test
    @DisplayName("Should successfully generate a valid JWT token string")
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should include extra custom claims in the generated token")
    void shouldGenerateTokenWithExtraClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_ADMIN");

        String token = jwtService.generateToken(claims, userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedRole = jwtService.extractClaim(token,
                c -> c.get("role", String.class));
        assertEquals("ROLE_ADMIN", extractedRole);
    }

    @Test
    @DisplayName("Should map the username to the token's subject.")
    void shouldMapUsernameFromUserDetails() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String subject = jwtService.extractUsername(token);
        assertNotNull(subject);
        assertEquals(username, subject);
    }

    @Test
    @DisplayName("Should throw ExpiredJwtException when token is past its expiration date")
    void shouldThrowExceptionWhenTokenIsExpired() {
        // 1. Create a frozen clock representing "Right Now"
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());

        // Initialize service with our frozen clock to generate the token
        jwtService = new JwtServiceImpl(frozenClock);
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        // 2. Fast-forward time into the future (e.g., 25 hours later)
        // Since your token expires in 24 hours, it should now be invalid
        Instant futureTime = now.plus(25, ChronoUnit.HOURS);
        Clock futureClock = Clock.fixed(futureTime, ZoneId.systemDefault());

        // Re-initialize the service with the future clock
        jwtService = new JwtServiceImpl(futureClock);

        // 3. Assert that parsing this token now throws the ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }
}
