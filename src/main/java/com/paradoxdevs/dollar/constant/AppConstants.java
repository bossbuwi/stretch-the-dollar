package com.paradoxdevs.dollar.constant;

import java.util.concurrent.TimeUnit;

public final class AppConstants {

    private AppConstants() {}

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_ROUTES = "/auth/**";
    public static final String HEALTH_ROUTE = "/health";
    public static final String H2_CONSOLE = "/h2-console/";
    public static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(1);
}
