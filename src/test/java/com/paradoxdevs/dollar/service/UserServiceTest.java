package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.UserMapper;
import com.paradoxdevs.dollar.repository.UserRepository;
import com.paradoxdevs.dollar.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.paradoxdevs.dollar.error.ErrorCode.RESOURCE_NOT_FOUND;
import static com.paradoxdevs.dollar.helper.UserDataHelper.createValidUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("Tests for getUsers()")
    class GetUsersTests {

        @Test
        @DisplayName("Successfully retrieve users.")
        public void shouldReturnAllUsers() {
            List<User> users = List.of(createValidUser());
            when(userRepository.findAll()).thenReturn(users);

            List<UserResponse> result = userService.getUsers();

            assertNotNull(result);
            assertEquals(users.size(), result.size());
            verify(userRepository, times(1)).findAll();
            verifyNoMoreInteractions(userRepository);
            verify(userMapper, times(1)).entityToResponse(users.getFirst());
        }

        @Test
        @DisplayName("User database is empty.")
        public void shouldReturnEmptyUsers() {
            List<User> users = new ArrayList<>();

            when(userRepository.findAll()).thenReturn(users);

            List<UserResponse> result = userService.getUsers();

            assertNotNull(result);
            assertEquals(0, result.size());
            verify(userRepository, times(1)).findAll();
            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(userRepository.findAll()).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUsers());

            assertEquals("Database connection timed out", exception.getMessage());
            verify(userRepository, times(1)).findAll();
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("Tests for getUserById()")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return a user when available.")
        void shouldReturnUserWhenAvailable() {
            User user = createValidUser();
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

            UserResponse result = userService.getUserById(user.getId());

            assertNotNull(result);
            assertEquals(user.getId(), Long.parseLong(result.getId()));
            verify(userRepository, times(1)).findById(user.getId());
            verifyNoMoreInteractions(userRepository);
            verify(userMapper, times(1)).entityToResponse(user);
        }

        @Test
        @DisplayName("Should return exception when user is not available.")
        void shouldReturnExceptionWhenUserIsNotAvailable() {
            User user = createValidUser();
            when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(user.getId()));

            assertInstanceOf(ResourceNotFoundException.class, ex);
            assertEquals(RESOURCE_NOT_FOUND.getErrorMessage(), ex.getMessage());
            verify(userRepository, times(1)).findById(user.getId());
            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(userRepository.findById(any())).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserById(anyLong()));

            assertEquals("Database connection timed out", exception.getMessage());
            verify(userRepository, times(1)).findById(anyLong());
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("Tests for getUserByUsername()")
    class GetUserByUsernameTests {

        @Test
        @DisplayName("Should return user if username exists.")
        void shouldReturnUserWhenUsernameExists() {
            User user = createValidUser();
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

            UserResponse result = userService.getUserByUsername(user.getUsername());

            assertNotNull(result);
            assertEquals(user.getUsername(), result.getUsername());
        }
    }

    @Nested
    @DisplayName("Tests for getUserByUuid()")
    class GetUserByUuidTests {

        @Test
        @DisplayName("Should return user if uuid exists.")
        void shouldReturnUserWhenUuidExists() {
            User user = createValidUser();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

            UserResponse result = userService.getUserByUuid(user.getUuid().toString());

            assertNotNull(result);
            assertEquals(user.getUuid().toString(), result.getUuid());
        }
    }

    @Nested
    @DisplayName("Tests for makeAdmin()")
    class MakeAdminTests {

        @Test
        @DisplayName("Should add admin role to user.")
        void shouldAddAdminRoleToUser() {
            User user = createValidUser();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            assertTrue(user.getRoles().contains(Role.USER));
            assertFalse(user.getRoles().contains(Role.ADMIN));

            userService.makeAdmin(user.getUuid().toString());

            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.ADMIN));
        }
    }

    @Nested
    @DisplayName("Tests for demoteAdmin()")
    class DemoteAdminTests {

        @Test
        @DisplayName("Should remove admin role from user.")
        void shouldRemoveAdminRoleFromUser() {
            User user = createValidUser();
            user.addRole(Role.ADMIN);
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.ADMIN));

            userService.demoteAdmin(user.getUuid().toString());

            assertFalse(user.getRoles().contains(Role.ADMIN));
            assertTrue(user.getRoles().contains(Role.USER));
        }
    }

    @Nested
    @DisplayName("Tests for restrictUser()")
    class RestrictUserTests {

        @Test
        @DisplayName("Should add restricted role to user.")
        void shouldAddRestrictedRoleToUser() {
            User user = createValidUser();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            assertTrue(user.getRoles().contains(Role.USER));
            assertFalse(user.getRoles().contains(Role.RESTRICTED));

            userService.restrictUser(user.getUuid().toString());

            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.RESTRICTED));
        }
    }

    @Nested
    @DisplayName("Tests for allowUser()")
    class AllowUserTests {

        @Test
        @DisplayName("Should remove restricted role from user.")
        void shouldRemoveAdminRoleFromUser() {
            User user = createValidUser();
            user.addRole(Role.RESTRICTED);
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.RESTRICTED));

            userService.allowUser(user.getUuid().toString());

            assertFalse(user.getRoles().contains(Role.RESTRICTED));
            assertTrue(user.getRoles().contains(Role.USER));
        }
    }

    @Nested
    @DisplayName("Tests for banUser()")
    class BanUserTests {

        @Test
        @DisplayName("Should add banned role to user.")
        void shouldAddBannedRoleToUser() {
            User user = createValidUser();
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            assertTrue(user.getRoles().contains(Role.USER));
            assertFalse(user.getRoles().contains(Role.BANNED));

            userService.banUser(user.getUuid().toString());

            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.BANNED));
        }
    }

    @Nested
    @DisplayName("Tests for unbanUser()")
    class UnbanUserTests {

        @Test
        @DisplayName("Should remove banned role from user.")
        void shouldRemoveAdminRoleFromUser() {
            User user = createValidUser();
            user.addRole(Role.BANNED);
            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.BANNED));

            userService.unbanUser(user.getUuid().toString());

            assertFalse(user.getRoles().contains(Role.BANNED));
            assertTrue(user.getRoles().contains(Role.USER));
        }
    }

    @Nested
    @DisplayName("Tests for resetUser()")
    class ResetUserTests {

        @Test
        @DisplayName("Should reset all user's role back to USER.")
        void shouldResetAllUserRoleBackToUser() {
            User user = createValidUser();
            user.addRole(Role.ADMIN);
            user.addRole(Role.RESTRICTED);
            user.addRole(Role.BANNED);

            assertTrue(user.getRoles().contains(Role.USER));
            assertTrue(user.getRoles().contains(Role.ADMIN));
            assertTrue(user.getRoles().contains(Role.RESTRICTED));
            assertTrue(user.getRoles().contains(Role.BANNED));

            when(userRepository.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

            userService.resetUser(user.getUuid().toString());

            assertFalse(user.getRoles().contains(Role.ADMIN));
            assertFalse(user.getRoles().contains(Role.RESTRICTED));
            assertFalse(user.getRoles().contains(Role.BANNED));
            assertTrue(user.getRoles().contains(Role.USER));
        }
    }
}
