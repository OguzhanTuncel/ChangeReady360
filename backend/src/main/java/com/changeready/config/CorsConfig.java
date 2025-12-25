package com.changeready.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SEC-007: CORS Configuration
 * Restricts Cross-Origin requests to explicitly allowed origins only
 */
@Configuration
public class CorsConfig {

	@Value("${app.cors.allowed-origins:http://localhost:4200}")
	private String allowedOrigins;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		
		// SEC-007: Only allow explicitly whitelisted origins
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
		
		// Allowed HTTP methods
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		
		// Allowed headers
		configuration.setAllowedHeaders(Arrays.asList(
			"Authorization", 
			"Content-Type", 
			"X-Requested-With", 
			"Accept", 
			"Origin"
		));
		
		// Exposed headers (visible to client)
		configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
		
		// Allow credentials (cookies, auth headers)
		configuration.setAllowCredentials(true);
		
		// Cache preflight response for 1 hour
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
