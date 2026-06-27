package com.paradoxdevs.dollar.aspect.annotation;

import com.paradoxdevs.dollar.constant.PermissionType;
import com.paradoxdevs.dollar.entity.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithPermission {
    Role[] allowedRole() default {};
    Role[] notAllowedRole() default {};
    PermissionType permission() default PermissionType.ALL;
}
