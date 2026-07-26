package com.paradoxdevs.dollar.aspect;

import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.constant.PermissionType;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.error.exception.FeatureDisabledException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PermissionAspectTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Dummy classes with annotated methods used by the tests
    public static class DummyNone {
        @WithPermission(permission = PermissionType.NONE)
        public void annotated(String id) {}
    }

    public static class DummySelf {
        @WithPermission(permission = PermissionType.SELF)
        public void annotated(String id) {}
    }

    public static class DummyNonSelf {
        @WithPermission(permission = PermissionType.NONSELF)
        public void annotated(String id) {}
    }

    public static class DummyAll {
        @WithPermission(permission = PermissionType.ALL)
        public void annotated(String id) {}
    }

    public static class DummyNotAllowedRole {
        @WithPermission(notAllowedRole = {Role.USER})
        public void annotated(String id) {}
    }

    private Authentication buildAuthWithUuidAndRoles(UUID userUuid, List<String> roles) {
        Authentication auth = mock(Authentication.class);
        java.util.List<SimpleGrantedAuthority> authList = roles.stream().map(SimpleGrantedAuthority::new).collect(java.util.stream.Collectors.toList());
        when(auth.getAuthorities()).thenReturn((java.util.Collection) authList);
        HashMap<String, String> principal = new HashMap<>();
        principal.put("uuid", userUuid.toString());
        when(auth.getPrincipal()).thenReturn(principal);
        return auth;
    }

    private ProceedingJoinPoint buildJoinPoint(Object target, String methodName, Class<?>[] paramTypes, Object[] args, Object proceedResult) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getName()).thenReturn(methodName);
        when(signature.getParameterTypes()).thenReturn(paramTypes);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(proceedResult);
        return joinPoint;
    }

    @Test
    void grantPermissions_allowsWhenAllChecksPass() throws Throwable {
        PermissionAspect aspect = new PermissionAspect();
        UUID user = UUID.randomUUID();
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = buildAuthWithUuidAndRoles(user, List.of("ROLE_USER"));
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        DummyAll target = new DummyAll();
        String arg = user.toString();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{String.class}, new Object[]{arg}, "ok");

        Object result = aspect.grantPermissions(jp);
        assertEquals("ok", result);
    }

    @Test
    void grantPermissions_featureDisabled_throwsFeatureDisabledException() throws Throwable {
        PermissionAspect aspect = new PermissionAspect();
        UUID user = UUID.randomUUID();
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = buildAuthWithUuidAndRoles(user, List.of("ROLE_USER"));
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        DummyNone target = new DummyNone();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{String.class}, new Object[]{user.toString()}, null);

        assertThrows(FeatureDisabledException.class, () -> aspect.grantPermissions(jp));
    }

    @Test
    void grantPermissions_selfPermission_deniesDifferentUser() throws Throwable {
        PermissionAspect aspect = new PermissionAspect();
        UUID user = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = buildAuthWithUuidAndRoles(user, List.of("ROLE_USER"));
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        DummySelf target = new DummySelf();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{String.class}, new Object[]{other.toString()}, null);

        assertThrows(AccessDeniedException.class, () -> aspect.grantPermissions(jp));
    }

    @Test
    void grantPermissions_nonSelfPermission_deniesSameUser() throws Throwable {
        PermissionAspect aspect = new PermissionAspect();
        UUID user = UUID.randomUUID();
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = buildAuthWithUuidAndRoles(user, List.of("ROLE_USER"));
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        DummyNonSelf target = new DummyNonSelf();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{String.class}, new Object[]{user.toString()}, null);

        assertThrows(AccessDeniedException.class, () -> aspect.grantPermissions(jp));
    }

    @Test
    void grantPermissions_prohibitedRole_throwsAccessDenied() throws Throwable {
        PermissionAspect aspect = new PermissionAspect();
        UUID user = UUID.randomUUID();
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = buildAuthWithUuidAndRoles(user, List.of("ROLE_USER", "ROLE_ADMIN"));
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        DummyNotAllowedRole target = new DummyNotAllowedRole();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{String.class}, new Object[]{user.toString()}, null);

        assertThrows(AccessDeniedException.class, () -> aspect.grantPermissions(jp));
    }
}
