package com.paradoxdevs.dollar.aspect;

import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.constant.PermissionType;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.error.exception.FeatureDisabledException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@Order(1)
public class PermissionAspect {

    @Pointcut("@annotation(com.paradoxdevs.dollar.aspect.annotation.WithPermission)")
    public void methodWithAnnotation() {}

    @Pointcut("@within(com.paradoxdevs.dollar.aspect.annotation.WithPermission)")
    public void classWithAnnotation() {}

    @Pointcut("execution(public * *(..))")
    public void publicMethods() {}

    @Pointcut("methodWithAnnotation() || (classWithAnnotation() && publicMethods())")
    public void checkPermission() {}

    @Around("checkPermission()")
    public Object grantPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass()
                .getMethod(signature.getName(), signature.getParameterTypes());
        WithPermission annotation = method.getAnnotation(WithPermission.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(WithPermission.class);
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        HashMap<String, String> principal = (HashMap<String, String>) auth.getPrincipal();
        UUID userUuid = UUID.fromString(principal.get("uuid"));

        Object[] args = joinPoint.getArgs();

        checkFeatureEnabled(annotation);
        checkSelfPermission(annotation, args, userUuid);
        checkNonSelfPermission(annotation, args, userUuid);
        checkProhibitedRoles(annotation, authorities);
        checkAllowedRoles();

        return joinPoint.proceed();
    }

    private void checkFeatureEnabled(WithPermission annotation) {
        if (annotation.permission() == PermissionType.NONE) {
            throw new FeatureDisabledException();
        }
    }

    private void checkSelfPermission(WithPermission annotation, Object[] args, UUID userUuid) {
        UUID inputUUid = UUID.fromString(args[0].toString());
        if (annotation.permission() == PermissionType.SELF) {
            if (!inputUUid.equals(userUuid)) {
                throw new AccessDeniedException("You are not allowed to perform this action");
            }
        }
    }

    private void checkNonSelfPermission(WithPermission annotation, Object[] args, UUID userUuid) {
        if (annotation.permission() == PermissionType.NONSELF) {
            UUID inputUUid = UUID.fromString(args[0].toString());
            if (inputUUid.equals(userUuid)) {
                throw new AccessDeniedException("You are not allowed to perform this action");
            }
        }
    }

    private void checkProhibitedRoles(WithPermission annotation, List<String> authorities) {
        Role[] notAllowedRole = annotation.notAllowedRole();
        if (haveMatchingRoles(notAllowedRole, authorities)) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }
    }

    private void checkAllowedRoles() {

    }

    private boolean haveMatchingRoles(Role[] roles, List<String> authorities) {
        List<Role> roleList = Arrays.stream(roles).toList();
        for (Role role : roleList) {
            if (authorities.stream().anyMatch(authority -> authority.equals(role.getAuthority()))) {
                return true;
            }
        }
        return false;
    }
}
