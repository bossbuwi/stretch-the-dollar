package com.paradoxdevs.dollar.aspect;

import com.paradoxdevs.dollar.aspect.annotation.CheckOwnership;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class OwnershipAspect {
    private final EntityManager entityManager;
    private final AuditorAware<UUID> auditorAware;
    private final ConcurrentHashMap<Class<?>, Method> createdByMethodCache = new ConcurrentHashMap<>();

    public OwnershipAspect(EntityManager entityManager, AuditorAware<UUID> auditorAware) {
        this.entityManager = entityManager;
        this.auditorAware = auditorAware;
    }

    @Pointcut("@annotation(com.paradoxdevs.dollar.aspect.annotation.CheckOwnership)")
    public void methodWithAnnotation() {}

    @Around("methodWithAnnotation()")
    public Object checkOwnership(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckOwnership annotation = method.getAnnotation(CheckOwnership.class);

        Long id = (Long) joinPoint.getArgs()[0];
        Class<?> entityClass = annotation.value();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
        if (isAdmin) {
            return joinPoint.proceed();
        }

        Object entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new ResourceNotFoundException(
                    "Entity of type " + entityClass.getSimpleName() + " with id " + id + " not found"
            );
        }
        UUID currentUserId = auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new AccessDeniedException("Current Auditor is null"));
        UUID entityOwnerId = extractCreatedBy(entity, entityClass);

        if (!currentUserId.equals(entityOwnerId)) {
            throw new AccessDeniedException(
                    "Current Auditor is not the owner of this " + entityClass.getSimpleName()
            );
        }

        return joinPoint.proceed();
    }

    private UUID extractCreatedBy(Object entity, Class<?> entityClass) {
        try {
            Method getter = createdByMethodCache.computeIfAbsent(entityClass, clazz -> {
                try {
                    return clazz.getMethod("getCreatedBy");
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(
                            "Entity " + clazz.getSimpleName() +
                                    " must have a public getCreatedBy() method for ownership checking", e);
                }
            });
            return (UUID) getter.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke getCreatedBy() on " + entityClass.getSimpleName(), e);
        }
    }
}
