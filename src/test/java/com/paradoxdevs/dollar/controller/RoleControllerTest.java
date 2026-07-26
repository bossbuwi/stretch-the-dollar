package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.response.RoleResponse;
import com.paradoxdevs.dollar.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoleControllerTest {

    @Test
    void getAllRoles_returnsListFromService() {
        RoleService service = mock(RoleService.class);
        List<RoleResponse> roles = Arrays.asList(new RoleResponse("ROLE_USER"), new RoleResponse("ROLE_ADMIN"));
        when(service.getAvailableRoles()).thenReturn(roles);

        RoleController controller = new RoleController(service);

        ResponseEntity<List<RoleResponse>> resp = controller.getAllRoles();

        assertEquals(200, resp.getStatusCode().value());
        assertSame(roles, resp.getBody());
        verify(service, times(1)).getAvailableRoles();
    }

    @Test
    void getAllRoles_returnsEmptyList() {
        RoleService service = mock(RoleService.class);
        when(service.getAvailableRoles()).thenReturn(List.of());

        RoleController controller = new RoleController(service);

        ResponseEntity<List<RoleResponse>> resp = controller.getAllRoles();

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(service).getAvailableRoles();
    }
}
