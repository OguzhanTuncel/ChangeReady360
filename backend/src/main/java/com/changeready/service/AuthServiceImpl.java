package com.changeready.service;

import com.changeready.dto.auth.LoginRequest;
import com.changeready.dto.auth.LoginResponse;
import com.changeready.entity.User;
import com.changeready.exception.UnauthorizedException;
import com.changeready.repository.UserRepository;
import com.changeready.security.JwtTokenProvider;
import com.changeready.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider tokenProvider;
	private final UserRepository userRepository;

	public AuthServiceImpl(
		AuthenticationManager authenticationManager,
		JwtTokenProvider tokenProvider,
		UserRepository userRepository
	) {
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public LoginResponse login(LoginRequest loginRequest) {
		try {
			// Find user by email
			User user = userRepository.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

			// Check if user is active
			if (!user.getActive()) {
				throw new UnauthorizedException("User account is deactivated");
			}

			// Check if company is active
			if (!user.getCompany().getActive()) {
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

			// Generate JWT token
			String token = tokenProvider.generateToken(authentication);
			UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

			// Build response
			LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
				userPrincipal.getId(),
				userPrincipal.getEmail(),
				userPrincipal.getRole().name(),
				userPrincipal.getCompanyId()
			);

			return new LoginResponse(token, "Bearer", userInfo);


		} catch (BadCredentialsException e) {
			throw new UnauthorizedException("Invalid credentials");
		}
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

