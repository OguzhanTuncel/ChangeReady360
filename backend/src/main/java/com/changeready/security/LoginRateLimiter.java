package com.changeready.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SEC-006: Simple rate limiter for login attempts
 * Prevents brute force attacks by limiting login attempts per IP/email
 */
@Component
public class LoginRateLimiter {

	private final Map<String, LoginAttempts> attemptCache = new ConcurrentHashMap<>();
	
	// Configuration
	private static final int MAX_ATTEMPTS = 5;
	private static final long LOCKOUT_DURATION_SECONDS = 900; // 15 minutes
	private static final long CLEANUP_INTERVAL_SECONDS = 3600; // 1 hour
	
	private Instant lastCleanup = Instant.now();

	/**
	 * Check if login is allowed for the given identifier (IP or email)
	 * @param identifier IP address or email
	 * @return true if login is allowed, false if rate limit exceeded
	 */
	public boolean isAllowed(String identifier) {
		cleanupIfNeeded();
		
		LoginAttempts attempts = attemptCache.get(identifier);
		if (attempts == null) {
			return true;
		}
		
		// Check if lockout has expired
		if (attempts.isLockedOut() && attempts.hasLockoutExpired()) {
			attemptCache.remove(identifier);
			return true;
		}
		
		return !attempts.isLockedOut();
	}

	/**
	 * Record a failed login attempt
	 * @param identifier IP address or email
	 */
	public void recordFailedAttempt(String identifier) {
		cleanupIfNeeded();
		
		attemptCache.compute(identifier, (key, existing) -> {
			if (existing == null) {
				return new LoginAttempts(1, Instant.now());
			}
			
			// If lockout expired, reset
			if (existing.hasLockoutExpired()) {
				return new LoginAttempts(1, Instant.now());
			}
			
			// Increment attempts
			int newAttempts = existing.attempts + 1;
			Instant lockoutUntil = newAttempts >= MAX_ATTEMPTS 
				? Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS)
				: existing.lockoutUntil;
				
			return new LoginAttempts(newAttempts, lockoutUntil);
		});
	}

	/**
	 * Reset login attempts after successful login
	 * @param identifier IP address or email
	 */
	public void resetAttempts(String identifier) {
		attemptCache.remove(identifier);
	}

	/**
	 * Get remaining lockout time in seconds
	 * @param identifier IP address or email
	 * @return seconds remaining, 0 if not locked out
	 */
	public long getRemainingLockoutSeconds(String identifier) {
		LoginAttempts attempts = attemptCache.get(identifier);
		if (attempts == null || !attempts.isLockedOut()) {
			return 0;
		}
		
		long remaining = attempts.lockoutUntil.getEpochSecond() - Instant.now().getEpochSecond();
		return Math.max(0, remaining);
	}

	private void cleanupIfNeeded() {
		if (Instant.now().isAfter(lastCleanup.plusSeconds(CLEANUP_INTERVAL_SECONDS))) {
			attemptCache.entrySet().removeIf(entry -> entry.getValue().hasLockoutExpired());
			lastCleanup = Instant.now();
		}
	}

	private static class LoginAttempts {
		final int attempts;
		final Instant lockoutUntil;

		LoginAttempts(int attempts, Instant lockoutUntil) {
			this.attempts = attempts;
			this.lockoutUntil = lockoutUntil;
		}

		boolean isLockedOut() {
			return attempts >= MAX_ATTEMPTS && Instant.now().isBefore(lockoutUntil);
		}

		boolean hasLockoutExpired() {
			return Instant.now().isAfter(lockoutUntil);
		}
	}
}

