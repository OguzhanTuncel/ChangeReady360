package com.changeready.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * SEC-011: Swagger/OpenAPI Configuration
 * Only enabled in dev and test profiles, disabled in production
 */
@Configuration
@Profile("!prod") // SEC-011: Disable Swagger in production profile
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("ChangeReady360 API")
				.version("1.0.0")
				.description("API Documentation for ChangeReady360 B2B Platform - Manual Onboarding System"))
			.addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
			.components(new Components()
				.addSecuritySchemes("bearer-jwt",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.in(SecurityScheme.In.HEADER)
						.name("Authorization")
						.description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")));
	}
}

