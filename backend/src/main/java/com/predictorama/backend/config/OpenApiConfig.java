package com.predictorama.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";
    private static final Set<String> PUBLIC_OPERATIONS = Set.of(
            operationKey(HttpMethod.GET, "/"),
            operationKey(HttpMethod.GET, "/health"),
            operationKey(HttpMethod.POST, "/api/auth/google"),
            operationKey(HttpMethod.POST, "/api/auth/login")
    );

    @Bean
    public OpenAPI predictoramaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Predict-o-rama API")
                        .description("REST API for Predict-o-rama.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer securityOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((method, operation) -> {
                        if (isPublicOperation(method, path)) {
                            return;
                        }

                        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
                    }));
        };
    }

    private static boolean isPublicOperation(PathItem.HttpMethod method, String path) {
        return PUBLIC_OPERATIONS.contains(operationKey(HttpMethod.valueOf(method.name()), path));
    }

    private static String operationKey(HttpMethod method, String path) {
        return method.name() + " " + path;
    }
}
