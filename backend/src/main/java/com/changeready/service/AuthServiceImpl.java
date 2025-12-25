package com.changeready.service;

import com.changeready.audit.AuditLogger;
import com.changeready.dto.auth.LoginRequest;
import com.changeready.dto.auth.LoginResponse;
import com.changeready.entity.User;
import com.changeready.exception.UnauthorizedException;
import com.changeready.repository.UserRepository;
import com.changeready.security.JwtTokenProvider;
import com.changeready.security.LoginRateLimiter;
import com.changeready.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final UserRepository userRepository;
	private final LoginRateLimiter rateLimiter;
	private final AuditLogger auditLogger;

	public AuthServiceImpl(
		AuthenticationManager authenticationManager,
		JwtTokenProvider tokenProvider,
		UserRepository userRepository,
		LoginRateLimiter rateLimiter,
		AuditLogger auditLogger
	) {
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
		this.userRepository = userRepository;
		this.rateLimiter = rateLimiter;
		this.auditLogger = auditLogger;
	}

	@Override
	@Transactional
	public LoginResponse login(LoginRequest loginRequest) {
		String clientIp = getClientIpAddress();
		String identifier = clientIp + ":" + loginRequest.getEmail();
		
		// SEC-006: Check rate limit
		if (!rateLimiter.isAllowed(identifier)) {
			long remainingSeconds = rateLimiter.getRemainingLockoutSeconds(identifier);
			auditLogger.logLoginFailure(loginRequest.getEmail(), clientIp, "Rate limit exceeded");
			throw new UnauthorizedException(
				"Too many failed login attempts. Please try again in " + 
				(remainingSeconds / 60) + " minutes."
			);
		}
		
		try {
			// Find user by email
			User user = userRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

			// Check if user is active
			if (!user.getActive()) {
				rateLimiter.recordFailedAttempt(identifier);
				auditLogger.logLoginFailure(loginRequest.getEmail(), clientIp, "User deactivated");
				throw new UnauthorizedException("User account is deactivated");
			}

			// Check if company is active
			if (!user.getCompany().getActive()) {
				rateLimiter.recordFailedAttempt(identifier);
				auditLogger.logLoginFailure(loginRequest.getEmail(), clientIp, "Company deactivated");
				throw new UnauthorizedException("Company account is deactivated");
			}

			// Authenticate user
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					loginRequest.getEmail(),
					loginRequest.getPassword()
				)
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			// SEC-006: Reset rate limit on successful login
			rateLimiter.resetAttempts(identifier);
			
			// Generate JWT token
			String token = tokenProvider.generateToken(authentication);
			UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
			
			// SEC-012: Audit log successful login
			auditLogger.logLoginSuccess(userPrincipal.getId(), userPrincipal.getRole().name(), clientIp);

			// Build response
			LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
				userPrincipal.getId(),
				userPrincipal.getEmail(),
				userPrincipal.getRole().name(),
				userPrincipal.getCompanyId()
			);

			return new LoginResponse(token, "Bearer", userInfo);

		} catch (BadCredentialsException e) {
			// SEC-006: Record failed attempt
			rateLimiter.recordFailedAttempt(identifier);
			auditLogger.logLoginFailure(loginRequest.getEmail(), clientIp, "Invalid credentials");
			throw new UnauthorizedException("Invalid credentials");
		}
	}
	
	private String getClientIpAddress() {
		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				String xForwardedFor = request.getHeader("X-Forwarded-For");
				if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
					return xForwardedFor.split(",")[0].trim();
				}
				return request.getRemoteAddr();
			}
		} catch (Exception e) {
			// Ignore
		}
		return "unknown";
	}

	@Override
	public void logout() {
		SecurityContextHolder.clearContext();
		// Client-side token removal is sufficient for this phase
		// Server-side token blacklisting can be added later if needed
	}

	@Override
	public boolean validatePassword(String password) {
		if (password == null || password.length() < 8) {
			return false;
		}

		boolean hasUpperCase = false;
		boolean hasLowerCase = false;
		boolean hasDigit = false;

		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c)) {
				hasUpperCase = true;
			} else if (Character.isLowerCase(c)) {
				hasLowerCase = true;
			} else if (Character.isDigit(c)) {
				hasDigit = true;
			}
		}

		return hasUpperCase && hasLowerCase && hasDigit;
	}
}

