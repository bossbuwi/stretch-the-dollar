package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Test
    void index_returnsListFromService() {
        UserService service = mock(UserService.class);
        List<UserResponse> users = Arrays.asList(new UserResponse(), new UserResponse());
        when(service.getUsers()).thenReturn(users);

        UserController controller = new UserController(service);
        ResponseEntity<List<UserResponse>> resp = controller.index();

        assertEquals(200, resp.getStatusCode().value());
        assertSame(users, resp.getBody());
        verify(service).getUsers();
    }

    @Test
    void getUserById_returnsUser() {
        UserService service = mock(UserService.class);
        UserResponse ur = new UserResponse();
        when(service.getUserById(1L)).thenReturn(ur);

        UserController controller = new UserController(service);
        ResponseEntity<UserResponse> resp = controller.getUserById(1L);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(ur, resp.getBody());
        verify(service).getUserById(1L);
    }

    @Test
    void getUserByUsername_returnsUser() {
        UserService service = mock(UserService.class);
        UserResponse ur = new UserResponse();
        when(service.getUserByUsername("bob")).thenReturn(ur);

        UserController controller = new UserController(service);
        ResponseEntity<UserResponse> resp = controller.getUserByUsername("bob");

        assertEquals(200, resp.getStatusCode().value());
        assertSame(ur, resp.getBody());
        verify(service).getUserByUsername("bob");
    }

    @Test
    void getUserByUuid_returnsUser() {
        UserService service = mock(UserService.class);
        UserResponse ur = new UserResponse();
        when(service.getUserByUuid("uuid123")).thenReturn(ur);

        UserController controller = new UserController(service);
        ResponseEntity<UserResponse> resp = controller.getUserByUuid("uuid123");

        assertEquals(200, resp.getStatusCode().value());
        assertSame(ur, resp.getBody());
        verify(service).getUserByUuid("uuid123");
    }

    @Test
    void makeAdmin_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.makeAdmin("u1");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).makeAdmin("u1");
    }

    @Test
    void demoteAdmin_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.demoteAdmin("u2");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).demoteAdmin("u2");
    }

    @Test
    void restrictUser_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.restrictUser("u3");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).restrictUser("u3");
    }

    @Test
    void allowUser_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.allowUser("u4");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).allowUser("u4");
    }

    @Test
    void banUser_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.banUser("u5");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).banUser("u5");
    }

    @Test
    void unbanUser_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.unbanUser("u6");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).unbanUser("u6");
    }

    @Test
    void resetUser_callsService_andReturnsNoContent() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<Boolean> resp = controller.resetUser("u7");
        assertEquals(204, resp.getStatusCode().value());
        verify(service).resetUser("u7");
    }
}
