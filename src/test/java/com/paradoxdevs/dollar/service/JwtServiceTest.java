package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
public class JwtServiceTest {
    @Mock
    private UserDetails userDetails;
    @Spy
    private Clock clock = Clock.systemDefaultZone();
    @InjectMocks
    private JwtServiceImpl jwtService;

    @Nested
    @DisplayName("Tests for generateToken()")
    class GenerateTokenTests {

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
        @DisplayName("Should map the username to the token's subject")
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
        @DisplayName("Should include roles in generated token")
        void shouldIncludeRolesInToken() {
            UserDetails testUser = org.springframework.security.core.userdetails.User
                    .withUsername(USERNAME)
                    .password("password")
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
        @DisplayName("Should include UUID in token when user is a User entity")
        void shouldIncludeUuidInTokenForUserEntity() {
            User user = new User();
            user.setUsername(USERNAME);
            user.setUuid(UUID.randomUUID());

            String token = jwtService.generateToken(user);
            String extractedUuid = jwtService.extractUuid(token);

            assertNotNull(extractedUuid);
            assertEquals(user.getUuid().toString(), extractedUuid);
        }

        @Test
        @DisplayName("Should not include UUID when user is generic UserDetails")
        void shouldNotIncludeUuidForGenericUserDetails() {
            when(userDetails.getUsername()).thenReturn(USERNAME);

            String token = jwtService.generateToken(userDetails);
            String extractedUuid = jwtService.extractUuid(token);

            assertNull(extractedUuid);
        }
    }

    @Nested
    @DisplayName("Tests for extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should extract username from valid token")
        void shouldExtractUsernameFromToken() {
            when(userDetails.getUsername()).thenReturn(USERNAME);

            String token = jwtService.generateToken(userDetails);
            String extractedUsername = jwtService.extractUsername(token);

            assertNotNull(extractedUsername);
            assertEquals(USERNAME, extractedUsername);
        }
    }

    @Nested
    @DisplayName("Tests for extractUuid()")
    class ExtractUuidTests {

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
        @DisplayName("Should return null when UUID is not in token")
        void shouldReturnNullWhenUuidNotInToken() {
            when(userDetails.getUsername()).thenReturn(USERNAME);

            String token = jwtService.generateToken(userDetails);
            String extractedUuid = jwtService.extractUuid(token);

            assertNull(extractedUuid);
        }
    }

    @Nested
    @DisplayName("Tests for extractRoles()")
    class ExtractRolesTests {

        @Test
        @DisplayName("Should extract roles from token")
        void shouldExtractRolesFromToken() {
            UserDetails testUser = org.springframework.security.core.userdetails.User
                    .withUsername(USERNAME)
                    .password("password")
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
        @DisplayName("Should return empty list when no roles in token")
        void shouldReturnEmptyListWhenNoRoles() {
            Map<String, Object> claims = new HashMap<>();

            String token = jwtService.generateToken(claims, userDetails);
            List<String> roles = jwtService.extractRoles(token);

            assertNotNull(roles);
            assertEquals(0, roles.size());
        }

        @Test
        @DisplayName("Should return empty list when roles claim is malformed")
        void shouldReturnEmptyListWhenRolesMalformed() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", "not_a_list");

            String token = jwtService.generateToken(claims, userDetails);
            List<String> roles = jwtService.extractRoles(token);

            assertNotNull(roles);
            assertEquals(0, roles.size());
        }
    }

    @Nested
    @DisplayName("Tests for extractClaim()")
    class ExtractClaimTests {

        @Test
        @DisplayName("Should extract custom claim from token")
        void shouldExtractCustomClaim() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("custom_key", "custom_value");

            String token = jwtService.generateToken(claims, userDetails);
            String customValue = jwtService.extractClaim(token,
                    c -> c.get("custom_key", String.class));

            assertNotNull(customValue);
            assertEquals("custom_value", customValue);
        }
    }

    @Nested
    @DisplayName("Tests for isTokenValid() without UserDetails")
    class TokenValidityTests {

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
        @DisplayName("Should return true for token with future expiration")
        void shouldReturnTrueForTokenWithFutureExpiration() {
            Instant now = Instant.now();
            Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(frozenClock);
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            boolean isValid = jwtService.isTokenValid(token);
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false when token is exactly at expiration")
        void shouldReturnFalseWhenTokenExactlyExpired() {
            Instant now = Instant.now();
            Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(frozenClock);
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            Instant expirationTime = now.plus(EXPIRATION_TIME, ChronoUnit.MILLIS);
            Clock expirationClock = Clock.fixed(expirationTime, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(expirationClock);

            boolean isValid = jwtService.isTokenValid(token);
            assertFalse(isValid, "Token at exact expiration should be invalid");
        }

        @Test
        @DisplayName("Should return false when token is just before expiration (boundary test)")
        void shouldReturnTrueWhenTokenJustBeforeExpiration() {
            Instant now = Instant.now();
            Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(frozenClock);
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            Instant almostExpiredTime = now.plus(EXPIRATION_TIME - 1000, ChronoUnit.MILLIS);
            Clock almostExpiredClock = Clock.fixed(almostExpiredTime, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(almostExpiredClock);

            boolean isValid = jwtService.isTokenValid(token);
            assertTrue(isValid, "Token just before expiration should be valid");
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
        @DisplayName("Should return false for null token")
        void shouldReturnFalseForNullToken() {
            boolean isValid = jwtService.isTokenValid(null);
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Tests for isTokenValid() with UserDetails")
    class TokenValidityWithUserTests {

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
        @DisplayName("Should return false when token is expired even if user matches")
        void shouldReturnFalseWhenTokenIsExpiredForUser() {
            Instant now = Instant.now();
            Clock frozenClock = Clock.fixed(now, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(frozenClock);

            when(userDetails.getUsername()).thenReturn(USERNAME);

            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            Instant futureTime = now.plus(25, ChronoUnit.HOURS);
            Clock futureClock = Clock.fixed(futureTime, ZoneId.systemDefault());
            jwtService = new JwtServiceImpl(futureClock);

            boolean isValid = jwtService.isTokenValid(token, userDetails);
            assertFalse(isValid, "Expired token should be invalid even for matching user");
        }

        @Test
        @DisplayName("Should return false for null token with UserDetails")
        void shouldReturnFalseForNullTokenWithUser() {
            boolean isValid = jwtService.isTokenValid(null, userDetails);
            assertFalse(isValid);
        }
    }
}
