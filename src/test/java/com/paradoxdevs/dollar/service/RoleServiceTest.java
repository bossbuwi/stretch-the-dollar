package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.response.RoleResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleService Tests")
public class RoleServiceTest {
    private final RoleService roleService = new RoleServiceImpl();

    @Nested
    @DisplayName("Tests for getAvailableRoles()")
    class GetAvailableRolesTests {

        @Test
        @DisplayName("Should return all available roles.")
        void shouldReturnAllAvailableRoles() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            assertNotNull(roles);
            assertEquals(Role.values().length, roles.size());
            assertEquals(4, roles.size());
        }

        @Test
        @DisplayName("Should return roles in correct order.")
        void shouldReturnRolesInCorrectOrder() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            assertEquals("ADMIN", roles.get(0).getRoleName());
            assertEquals("USER", roles.get(1).getRoleName());
            assertEquals("RESTRICTED", roles.get(2).getRoleName());
            assertEquals("BANNED", roles.get(3).getRoleName());
        }

        @Test
        @DisplayName("Should return list with correct role names.")
        void shouldReturnListWithCorrectRoleNames() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            assertTrue(roles.stream().anyMatch(r -> "ADMIN".equals(r.getRoleName())));
            assertTrue(roles.stream().anyMatch(r -> "USER".equals(r.getRoleName())));
            assertTrue(roles.stream().anyMatch(r -> "RESTRICTED".equals(r.getRoleName())));
            assertTrue(roles.stream().anyMatch(r -> "BANNED".equals(r.getRoleName())));
        }

        @Test
        @DisplayName("Should return mutable list.")
        void shouldReturnMutableList() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            assertDoesNotThrow(() -> roles.add(new RoleResponse("TEST")));
            assertEquals(5, roles.size());
        }

        @Test
        @DisplayName("Should return non-empty list.")
        void shouldReturnNonEmptyList() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            assertNotNull(roles);
            assertFalse(roles.isEmpty());
            assertTrue(roles.size() > 0);
        }

        @Test
        @DisplayName("Should return new list on each call.")
        void shouldReturnNewListOnEachCall() {
            List<RoleResponse> roles1 = roleService.getAvailableRoles();
            List<RoleResponse> roles2 = roleService.getAvailableRoles();

            assertNotSame(roles1, roles2);
            assertEquals(roles1.size(), roles2.size());
        }

        @Test
        @DisplayName("Should have non-null role names.")
        void shouldHaveNonNullRoleNames() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            for (RoleResponse role : roles) {
                assertNotNull(role.getRoleName());
                assertFalse(role.getRoleName().isEmpty());
            }
        }

        @Test
        @DisplayName("Should have unique role names.")
        void shouldHaveUniqueRoleNames() {
            List<RoleResponse> roles = roleService.getAvailableRoles();
            long uniqueNames = roles.stream()
                    .map(RoleResponse::getRoleName)
                    .distinct()
                    .count();

            assertEquals(roles.size(), uniqueNames);
        }

        @Test
        @DisplayName("Should return matching count of roles and Role enum values.")
        void shouldReturnMatchingCountOfRolesAndRoleEnumValues() {
            List<RoleResponse> roles = roleService.getAvailableRoles();
            Role[] roleValues = Role.values();

            assertEquals(roleValues.length, roles.size());
        }

        @Test
        @DisplayName("Should contain role response objects with correct properties.")
        void shouldContainRoleResponseObjectsWithCorrectProperties() {
            List<RoleResponse> roles = roleService.getAvailableRoles();

            for (RoleResponse role : roles) {
                assertNotNull(role);
                assertNotNull(role.getRoleName());
                assertTrue(role.getRoleName().matches("[A-Z_]+"));
            }
        }
    }
}
