package com.predictorama.backend.config;

import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Single source of truth for unauthenticated API routes.
 * Both SecurityConfig (runtime enforcement) and OpenApiConfig (Swagger display)
 * derive their public-route sets from here so they can never drift apart.
 */
public final class PublicRoutes {

    public record Route(HttpMethod method, String path) {}

    public static final List<Route> ENTRIES = List.of(
            new Route(HttpMethod.GET, "/"),
            new Route(HttpMethod.GET, "/health"),
            new Route(HttpMethod.POST, "/api/auth/google"),
            new Route(HttpMethod.POST, "/api/auth/login")
    );

    private PublicRoutes() {}
}
