package com.changeready.controller;

import com.changeready.dto.auth.LoginRequest;
import com.changeready.dto.auth.LoginResponse;
import com.changeready.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		LoginResponse response = authService.login(loginRequest);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		authService.logout();
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}

