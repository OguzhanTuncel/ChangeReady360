package com.changeready.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * SEC-011: Swagger/OpenAPI Configuration
 * Only enabled in dev and test profiles, disabled in production
 */
@Configuration
@Profile("!prod") // SEC-011: Disable Swagger in production profile
public class SwaggerConfig {

	@Value("${spring.application.name:ChangeReady360}")
	private String applicationName;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title(applicationName + " API")
				.version("1.0")
				.description("REST API for ChangeReady360 - Manual Onboarding System"))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
			.components(new Components()
				.addSecuritySchemes("bearerAuth", new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")));
	}
}

