package com.paradoxdevs.dollar.aspect;

import com.paradoxdevs.dollar.aspect.annotation.CheckOwnership;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OwnershipAspectTest {

    public static class DummyEntity {
        private UUID createdBy;
        public DummyEntity(UUID createdBy) { this.createdBy = createdBy; }
        public UUID getCreatedBy() { return createdBy; }
    }

    public static class DummyService {
        @CheckOwnership(DummyEntity.class)
        public void annotated(Long id) {}
    }

    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    private SecurityContext buildSecurityContextWithRoles(String... roles) {
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        java.util.List<org.springframework.security.core.GrantedAuthority> authList = java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(java.util.stream.Collectors.toList());
        when(auth.getAuthorities()).thenReturn((java.util.Collection) authList);
        when(sc.getAuthentication()).thenReturn(auth);
        return sc;
    }

    private ProceedingJoinPoint buildJoinPoint(Object target, String methodName, Class<?>[] paramTypes, Object[] args) throws NoSuchMethodException {
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(DummyService.class.getMethod("annotated", Long.class));
        when(sig.getName()).thenReturn(methodName);
        when(sig.getParameterTypes()).thenReturn(paramTypes);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getTarget()).thenReturn(target);
        when(jp.getArgs()).thenReturn(args);
        try { when(jp.proceed()).thenReturn(null); } catch (Throwable ignored) {}
        return jp;
    }

    @Test
    void checkOwnership_allowsAdmin() throws Throwable {
        EntityManager em = mock(EntityManager.class);
        AuditorAware<UUID> auditor = mock(AuditorAware.class);
        OwnershipAspect aspect = new OwnershipAspect(em, auditor);

        SecurityContext sc = buildSecurityContextWithRoles("ROLE_ADMIN");
        SecurityContextHolder.setContext(sc);

        DummyService target = new DummyService();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{Long.class}, new Object[]{1L});

        // should not throw
        aspect.checkOwnership(jp);
    }

    @Test
    void checkOwnership_throwsNotFound_whenEntityMissing() throws Throwable {
        EntityManager em = mock(EntityManager.class);
        AuditorAware<UUID> auditor = mock(AuditorAware.class);
        OwnershipAspect aspect = new OwnershipAspect(em, auditor);

        SecurityContext sc = buildSecurityContextWithRoles();
        SecurityContextHolder.setContext(sc);

        when(em.find(DummyEntity.class, 1L)).thenReturn(null);

        DummyService target = new DummyService();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{Long.class}, new Object[]{1L});

        assertThrows(ResourceNotFoundException.class, () -> aspect.checkOwnership(jp));
    }

    @Test
    void checkOwnership_throwsAccessDenied_whenNotOwner() throws Throwable {
        EntityManager em = mock(EntityManager.class);
        AuditorAware<UUID> auditor = mock(AuditorAware.class);
        OwnershipAspect aspect = new OwnershipAspect(em, auditor);

        SecurityContext sc = buildSecurityContextWithRoles();
        SecurityContextHolder.setContext(sc);

        UUID owner = UUID.randomUUID();
        UUID current = UUID.randomUUID();
        when(em.find(DummyEntity.class, 1L)).thenReturn(new DummyEntity(owner));
        when(auditor.getCurrentAuditor()).thenReturn(Optional.of(current));

        DummyService target = new DummyService();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{Long.class}, new Object[]{1L});

        assertThrows(AccessDeniedException.class, () -> aspect.checkOwnership(jp));
    }

    @Test
    void checkOwnership_allowsOwner() throws Throwable {
        EntityManager em = mock(EntityManager.class);
        AuditorAware<UUID> auditor = mock(AuditorAware.class);
        OwnershipAspect aspect = new OwnershipAspect(em, auditor);

        SecurityContext sc = buildSecurityContextWithRoles();
        SecurityContextHolder.setContext(sc);

        UUID owner = UUID.randomUUID();
        when(em.find(DummyEntity.class, 1L)).thenReturn(new DummyEntity(owner));
        when(auditor.getCurrentAuditor()).thenReturn(Optional.of(owner));

        DummyService target = new DummyService();
        ProceedingJoinPoint jp = buildJoinPoint(target, "annotated", new Class[]{Long.class}, new Object[]{1L});

        // should not throw
        aspect.checkOwnership(jp);
    }
}
