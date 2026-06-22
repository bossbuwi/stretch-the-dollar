package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.service.impl.JwtServiceImpl;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.paradoxdevs.dollar.constant.AppConstants.EXPIRATION_TIME;
import static com.paradoxdevs.dollar.helper.JwtDataHelper.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @Mock
    private UserDetails userDetails;
    @Spy
    private Clock clock = Clock.systemDefaultZone();
    @InjectMocks
    private JwtServiceImpl jwtService;

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
        when(userDetails.getUsername()).thenReturn(USERNAME);

        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String subject = jwtService.extractUsername(token);
        assertNotNull(subject);
        assertEquals(USERNAME, subject);
    }

    @Test
    @DisplayName("Should return false when token is past its expiration date")
    void shouldReturnFalseWhenTokenIsExpired() {
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(frozenClock);
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        Instant futureTime = now.plus(25, ChronoUnit.HOURS);
        Clock futureClock = Clock.fixed(futureTime, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(futureClock);

        boolean isValid = jwtService.isTokenValid(token);
        assertFalse(isValid, "Token should be invalid when expired");
    }

    @Test
    @DisplayName("Should return true when token is valid and not expired")
    void shouldReturnTrueWhenTokenIsValid() {
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(frozenClock);
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        boolean isValid = jwtService.isTokenValid(token);
        assertTrue(isValid, "Token should be valid");
    }

    @Test
    @DisplayName("Should return true when token belongs to the user and is not expired")
    void shouldReturnTrueWhenTokenBelongsToUser() {
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(frozenClock);

        when(userDetails.getUsername()).thenReturn(USERNAME);

        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false when token doesn't belong to the user")
    void shouldReturnFalseWhenTokenDoesNotBelongToUser() {
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(frozenClock);
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("different_user");

        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract UUID from token when user is a User entity")
    void shouldExtractUuidFromToken() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setUuid(UUID.randomUUID());

        String token = jwtService.generateToken(user);
        String extractedUuid = jwtService.extractUuid(token);

        assertNotNull(extractedUuid);
        assertEquals(user.getUuid().toString(), extractedUuid);
    }

    @Test
    @DisplayName("Should extract roles from token")
    void shouldExtractRolesFromToken() {
        UserDetails testUser = org.springframework.security.core.userdetails.User
                .withUsername(USERNAME)
                .password("password")  // You can use any dummy password
                .authorities("ROLE_USER", "ROLE_ADMIN")
                .build();

        String token = jwtService.generateToken(testUser);
        List<String> roles = jwtService.extractRoles(token);

        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should return false for malformed token")
    void shouldReturnFalseForMalformedToken() {
        String malformedToken = "invalid.token.here";
        boolean isValid = jwtService.isTokenValid(malformedToken);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should return false for empty token")
    void shouldReturnFalseForEmptyToken() {
        boolean isValid = jwtService.isTokenValid("");
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should return true for token with future expiration when using fixed clock")
    void shouldReturnTrueForTokenWithFutureExpiration() {
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(frozenClock);
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        // Use the same clock (token not expired yet)
        boolean isValid = jwtService.isTokenValid(token);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false when token is exactly at expiration")
    void shouldReturnFalseWhenTokenExactlyExpired() throws InterruptedException {
        Instant now = Instant.now();
        Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(frozenClock);
        String token = jwtService.generateToken(new HashMap<>(), userDetails);

        // Move clock to exactly expiration time
        Instant expirationTime = now.plus(EXPIRATION_TIME, ChronoUnit.MILLIS);
        Clock expirationClock = Clock.fixed(expirationTime, ZoneId.systemDefault());
        jwtService = new JwtServiceImpl(expirationClock);

        boolean isValid = jwtService.isTokenValid(token);
        assertFalse(isValid, "Token at exact expiration should be invalid");
    }
}
