package com.changeready.security;

import com.changeready.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

	private final SecretKey secretKey;
	private final long expirationInMs;

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration}") long expirationInMs
	) {
		// SEC-001: Validate JWT secret length (must be at least 256 bits = 32 bytes)
		if (secret == null || secret.isEmpty()) {
			throw new IllegalArgumentException("JWT secret cannot be null or empty. Please set JWT_SECRET environment variable.");
		}
		
		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalArgumentException(
				"JWT secret must be at least 32 bytes (256 bits) long for HS256. Current length: " + secretBytes.length + " bytes. " +
				"Please set a stronger JWT_SECRET environment variable."
			);
		}
		
		this.secretKey = Keys.hmacShaKeyFor(secretBytes);
		this.expirationInMs = expirationInMs;
	}

	public String generateToken(Authentication authentication) {
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		Instant now = Instant.now();
		Instant expiryDate = now.plusMillis(expirationInMs);

		// SEC-005: Explicitly use HS256 algorithm for token signing
		return Jwts.builder()
			.subject(userPrincipal.getEmail())
			.claim("id", userPrincipal.getId())
			.claim("role", userPrincipal.getRole().name())
			.claim("companyId", userPrincipal.getCompanyId())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiryDate))
			.signWith(secretKey, Jwts.SIG.HS256) // Explicitly set algorithm
			.compact();
	}

	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(secretKey)
			.clockSkewSeconds(60) // SEC-005: Allow 60 seconds clock skew for distributed systems
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return claims.getSubject();
	}

	public Role getRoleFromToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(secretKey)
			.clockSkewSeconds(60) // SEC-005: Allow 60 seconds clock skew
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return Role.valueOf(claims.get("role", String.class));
	}

	public Long getCompanyIdFromToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(secretKey)
			.clockSkewSeconds(60) // SEC-005: Allow 60 seconds clock skew
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return claims.get("companyId", Long.class);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.clockSkewSeconds(60) // SEC-005: Allow 60 seconds clock skew
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}

