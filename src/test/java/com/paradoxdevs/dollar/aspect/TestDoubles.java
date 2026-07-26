package com.paradoxdevs.dollar.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public class TestDoubles {

    public static class FakeMethodSignature implements MethodSignature {
        private final Method method;

        public FakeMethodSignature(Method method) {
            this.method = method;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public String[] getParameterNames() {
            Class<?>[] pts = method.getParameterTypes();
            String[] names = new String[pts.length];
            for (int i = 0; i < pts.length; i++) names[i] = "arg" + i;
            return names;
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        @Override
        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return method.getExceptionTypes();
        }

        @Override
        public int getModifiers() {
            return method.getModifiers();
        }

        @Override
        public Class getDeclaringType() {
            return method.getDeclaringClass();
        }

        @Override
        public String getDeclaringTypeName() {
            return method.getDeclaringClass().getName();
        }

        @Override
        public String getName() {
            return method.getName();
        }

        @Override
        public String toShortString() {
            return method.toString();
        }

        @Override
        public String toLongString() {
            return method.toString();
        }
    }

    public static ProceedingJoinPoint createProceedingJoinPointProxy(Object target, Method method, Object[] args, Object proceedResult, Throwable toThrow) {
        MethodSignature sig = new FakeMethodSignature(method);
        java.lang.reflect.InvocationHandler handler = (proxy, m, params) -> {
            String name = m.getName();
            switch (name) {
                case "proceed":
                    if (toThrow != null) throw toThrow;
                    return proceedResult;
                case "getSignature":
                    return sig;
                case "getTarget":
                case "getThis":
                    return target;
                case "getArgs":
                    return args == null ? new Object[0] : args;
                case "toShortString":
                    return sig.toShortString();
                case "toLongString":
                    return sig.toLongString();
                case "getStaticPart":
                    return null;
                default:
                    // ignore other interface methods
                    return null;
            }
        };
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                ProceedingJoinPoint.class.getClassLoader(),
                new Class[]{ProceedingJoinPoint.class},
                handler
        );
        return (ProceedingJoinPoint) proxy;
    }
}
