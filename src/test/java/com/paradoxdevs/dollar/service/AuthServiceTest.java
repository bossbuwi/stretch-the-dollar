package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.api.request.PasswordRequest;
import com.paradoxdevs.dollar.api.response.AuthResponse;
import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.exception.PasswordException;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.AuthMapper;
import com.paradoxdevs.dollar.repository.UserRepository;
import com.paradoxdevs.dollar.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.paradoxdevs.dollar.helper.UserDataHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);
    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("Tests for register()")
    class RegisterTests {

        @Test
        @DisplayName("Successfully register new user.")
        void shouldRegisterNewUser() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            User user = new User();
            user.setUsername(USERNAME);
            user.setPassword(PASSWORD);

            User registeredUser = createValidUser();
            String encodedPassword = "encodedPassword123";

            when(authMapper.requestToEntity(request)).thenReturn(user);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(registeredUser);

            UserResponse result = authService.register(request);

            assertNotNull(result);
            assertEquals(registeredUser.getUsername(), result.getUsername());
            assertTrue(registeredUser.getRoles().contains(Role.USER));
            verify(authMapper, times(1)).requestToEntity(request);
            verify(passwordEncoder, times(1)).encode(PASSWORD);
            verify(userRepository, times(1)).save(any(User.class));
            verify(authMapper, times(1)).entityToResponseNoId(registeredUser);
        }

        @Test
        @DisplayName("Should encode password during registration.")
        void shouldEncodePasswordDuringRegistration() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            User user = new User();
            user.setUsername(USERNAME);
            user.setPassword(PASSWORD);

            User registeredUser = createValidUser();
            String encodedPassword = "encodedPassword123";

            when(authMapper.requestToEntity(request)).thenReturn(user);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(registeredUser);

            authService.register(request);

            verify(passwordEncoder, times(1)).encode(PASSWORD);
            assertEquals(encodedPassword, user.getPassword());
        }

        @Test
        @DisplayName("Should add USER role during registration.")
        void shouldAddUserRoleDuringRegistration() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            User user = new User();
            user.setUsername(USERNAME);
            user.setPassword(PASSWORD);

            User registeredUser = createValidUser();

            when(authMapper.requestToEntity(request)).thenReturn(user);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenReturn(registeredUser);

            authService.register(request);

            assertTrue(user.getRoles().contains(Role.USER));
        }

        @Test
        @DisplayName("Should propagate exception when mapper fails.")
        void shouldPropagateExceptionWhenMapperFails() {
            AuthRequest request = new AuthRequest();
            RuntimeException mapperException = new RuntimeException("Mapping error");
            when(authMapper.requestToEntity(request)).thenThrow(mapperException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

            assertEquals("Mapping error", exception.getMessage());
            verify(authMapper, times(1)).requestToEntity(request);
            verifyNoInteractions(passwordEncoder, userRepository);
        }

        @Test
        @DisplayName("Should propagate exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryFails() {
            AuthRequest request = new AuthRequest();
            User user = new User();
            user.setUsername(USERNAME);
            user.setPassword(PASSWORD);

            RuntimeException repositoryException = new RuntimeException("Database error");
            when(authMapper.requestToEntity(request)).thenReturn(user);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenThrow(repositoryException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

            assertEquals("Database error", exception.getMessage());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Tests for login()")
    class LoginTests {

        @Test
        @DisplayName("Successfully login user and return token.")
        void shouldSuccessfullyLoginUserAndReturnToken() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            User user = createValidUser();
            String token = "jwt_token_12345";

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(jwtService.generateToken(user)).thenReturn(token);

            AuthResponse result = authService.login(request);

            assertNotNull(result);
            assertEquals(token, result.getToken());
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService, times(1)).generateToken(user);
        }

        @Test
        @DisplayName("Should pass correct credentials to authentication manager.")
        void shouldPassCorrectCredentialsToAuthenticationManager() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            User user = createValidUser();
            String token = "jwt_token_12345";

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(jwtService.generateToken(user)).thenReturn(token);

            authService.login(request);

            verify(authenticationManager).authenticate(argThat(token_arg -> 
                    token_arg.getName().equals(USERNAME) && 
                    token_arg.getCredentials().equals(PASSWORD)
            ));
        }

        @Test
        @DisplayName("Should throw exception when authentication fails.")
        void shouldThrowExceptionWhenAuthenticationFails() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword("wrongPassword");

            RuntimeException authException = new RuntimeException("Invalid credentials");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(authException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

            assertEquals("Invalid credentials", exception.getMessage());
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("Should throw exception when token generation fails.")
        void shouldThrowExceptionWhenTokenGenerationFails() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            User user = createValidUser();

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);

            RuntimeException tokenException = new RuntimeException("Token generation failed");
            when(jwtService.generateToken(user)).thenThrow(tokenException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

            assertEquals("Token generation failed", exception.getMessage());
            verify(jwtService, times(1)).generateToken(user);
        }
    }

    @Nested
    @DisplayName("Tests for changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Successfully change password with valid credentials.")
        void shouldSuccessfullyChangePassword() {
            PasswordRequest request = new PasswordRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);
            request.setNewPassword("newPassword123");
            request.setConfirmNewPassword("newPassword123");

            User user = createValidUser();
            String encodedNewPassword = "encodedNewPassword123";

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD)).thenReturn(true);
            when(passwordEncoder.matches("newPassword123", PASSWORD)).thenReturn(false);
            when(passwordEncoder.encode("newPassword123")).thenReturn(encodedNewPassword);
            when(userRepository.save(user)).thenReturn(user);

            authService.changePassword(request);

            assertEquals(encodedNewPassword, user.getPassword());
            verify(userRepository, times(1)).findByUsername(USERNAME);
            verify(passwordEncoder, times(1)).encode("newPassword123");
            verify(userRepository, times(1)).save(user);
        }

        @Test
        @DisplayName("Should throw exception when user not found.")
        void shouldThrowExceptionWhenUserNotFound() {
            PasswordRequest request = new PasswordRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);
            request.setNewPassword("newPassword123");
            request.setConfirmNewPassword("newPassword123");

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> authService.changePassword(request));

            verify(userRepository, times(1)).findByUsername(USERNAME);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect.")
        void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
            PasswordRequest request = new PasswordRequest();
            request.setUsername(USERNAME);
            request.setPassword("wrongPassword");
            request.setNewPassword("newPassword123");
            request.setConfirmNewPassword("newPassword123");

            User user = createValidUser();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

            PasswordException exception = assertThrows(PasswordException.class,
                    () -> authService.changePassword(request));

            verify(userRepository, times(1)).findByUsername(USERNAME);
            verify(passwordEncoder, times(1)).matches("wrongPassword", user.getPassword());
            verifyNoMoreInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Should throw exception when new password matches old password.")
        void shouldThrowExceptionWhenNewPasswordMatchesOldPassword() {
            PasswordRequest request = new PasswordRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);
            request.setNewPassword(PASSWORD);
            request.setConfirmNewPassword(PASSWORD);

            User user = createValidUser();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches(PASSWORD, user.getPassword())).thenReturn(true);

            PasswordException exception = assertThrows(PasswordException.class,
                    () -> authService.changePassword(request));

            assertEquals(ErrorCode.OLD_NEW_PASSWORD_MATCH, exception.getErrorCode());
            verify(userRepository, times(1)).findByUsername(USERNAME);
            verify(passwordEncoder, times(2)).matches(PASSWORD, user.getPassword());
        }

        @Test
        @DisplayName("Should throw exception when new password and confirm password don't match.")
        void shouldThrowExceptionWhenNewPasswordAndConfirmPasswordDontMatch() {
            PasswordRequest request = new PasswordRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);
            request.setNewPassword("newPassword123");
            request.setConfirmNewPassword("differentPassword");

            User user = createValidUser();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("newPassword123", user.getPassword())).thenReturn(false);

            PasswordException exception = assertThrows(PasswordException.class,
                    () -> authService.changePassword(request));

            assertEquals(ErrorCode.PASSWORDS_DONT_MATCH, exception.getErrorCode());
            verify(userRepository, times(1)).findByUsername(USERNAME);
            verify(userRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should throw exception when repository fails to find user.")
        void shouldThrowExceptionWhenRepositoryFailsToFindUser() {
            PasswordRequest request = new PasswordRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);
            request.setNewPassword("newPassword123");
            request.setConfirmNewPassword("newPassword123");

            RuntimeException repositoryException = new RuntimeException("Database connection failed");
            when(userRepository.findByUsername(USERNAME)).thenThrow(repositoryException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.changePassword(request));

            assertEquals("Database connection failed", exception.getMessage());
            verify(userRepository, times(1)).findByUsername(USERNAME);
        }
    }

    @Nested
    @DisplayName("Tests for forgetPassword()")
    class ForgetPasswordTests {

        @Test
        @DisplayName("Forget password method executes without error.")
        void shouldExecuteForgetPasswordWithoutError() {
            AuthRequest request = new AuthRequest();
            request.setUsername(USERNAME);

            assertDoesNotThrow(() -> authService.forgetPassword(request));
        }
    }
}
