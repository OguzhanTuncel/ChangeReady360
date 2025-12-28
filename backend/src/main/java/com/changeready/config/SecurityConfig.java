package com.changeready.config;

import com.changeready.security.JwtAccessDeniedHandler;
import com.changeready.security.JwtAuthenticationEntryPoint;
import com.changeready.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	private final CorsConfigurationSource corsConfigurationSource;

	public SecurityConfig(
		JwtAuthenticationFilter jwtAuthenticationFilter,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		JwtAccessDeniedHandler jwtAccessDeniedHandler,
		CorsConfigurationSource corsConfigurationSource
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// SEC-002: Use BCrypt with strength 12 (2^12 = 4096 iterations)
		// Higher strength = more secure but slower. 12 is recommended for production.
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// CSRF disabled for stateless JWT authentication
			.csrf(AbstractHttpConfigurer::disable)
			
			// SEC-007: Apply CORS configuration
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			
			// Stateless session management (JWT-based)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			
			// Authorization rules
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/index.html").permitAll() // Swagger UI - muss zuerst kommen
				.requestMatchers("/api/v1/auth/login", "/api/v1/auth/logout").permitAll() // Öffentliche Auth-Endpoints
				.requestMatchers("/api/v1/company-access-requests").permitAll() // Öffentlicher POST-Endpoint
				.requestMatchers("/api/v1/company-access-requests/**").authenticated() // Geschützte GET/PUT-Endpoints
				.requestMatchers("/api/v1/admin/**").authenticated() // Admin-Endpoints
				.requestMatchers("/api/v1/dashboard/**").authenticated() // Dashboard-Endpoints (geschützt durch @PreAuthorize)
				.requestMatchers("/api/v1/stakeholder/**").authenticated() // Stakeholder-Endpoints (geschützt durch @PreAuthorize)
				.requestMatchers("/api/v1/reporting/**").authenticated() // Reporting-Endpoints (geschützt durch @PreAuthorize)
				.requestMatchers("/api/v1/measures/**").authenticated() // Measures-Endpoints (geschützt durch @PreAuthorize)
				.requestMatchers("/api/v1/surveys/**").authenticated() // Survey-Endpoints (geschützt durch @PreAuthorize)
				.anyRequest().permitAll()
			)
			
			// Exception handling
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			
			// SEC-008: Security Headers
			.headers(headers -> headers
				// XSS Protection
				.xssProtection(xss -> {})
				
				// Content Security Policy
				.contentSecurityPolicy(csp -> csp.policyDirectives(
					"default-src 'self'; " +
					"script-src 'self' 'unsafe-inline'; " +
					"style-src 'self' 'unsafe-inline'; " +
					"img-src 'self' data:; " +
					"font-src 'self'; " +
					"connect-src 'self';"
				))
				
				// Prevent Clickjacking
				.frameOptions(frameOptions -> frameOptions.deny())
				
				// Referrer Policy
				.referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
				
				// HSTS - Force HTTPS (1 year, include subdomains)
				.httpStrictTransportSecurity(hsts -> hsts
					.includeSubDomains(true)
					.maxAgeInSeconds(31536000)
				)
				
				// Prevent MIME-Type Sniffing
				.contentTypeOptions(contentType -> {})
			)
			
			// Add JWT filter
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}

