package com.changeready.controller;

import com.changeready.dto.user.UserRequest;
import com.changeready.dto.user.UserResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();

		UserResponse response = userService.create(request, currentUser.getCompanyId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();

		List<UserResponse> users = userService.findAllByCompany(currentUser.getCompanyId());
		return ResponseEntity.ok(users);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();

		UserResponse user = userService.findById(id, currentUser.getCompanyId());
		return ResponseEntity.ok(user);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponse> updateUser(
		@PathVariable Long id,
		@Valid @RequestBody UserRequest request
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();

		UserResponse response = userService.update(id, request, currentUser.getCompanyId());
		return ResponseEntity.ok(response);
	}
}

