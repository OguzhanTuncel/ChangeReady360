package com.changeready.controller;

import com.changeready.entity.Company;
import com.changeready.entity.Role;
import com.changeready.entity.User;
import com.changeready.repository.CompanyRepository;
import com.changeready.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin-setup")
public class AdminSetupController {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${admin.initial.email}")
	private String initialAdminEmail;

	@Value("${admin.initial.password}")
	private String initialAdminPassword;

	public AdminSetupController(
		UserRepository userRepository,
		CompanyRepository companyRepository,
		PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/check-admin")
	@Transactional(readOnly = true)
	public ResponseEntity<Map<String, Object>> checkAdminUser() {
		Map<String, Object> response = new HashMap<>();
		
		try {
			User adminUser = userRepository.findAll().stream()
				.filter(user -> user.getRole() == Role.SYSTEM_ADMIN)
				.findFirst()
				.orElse(null);
			
			if (adminUser == null) {
				response.put("exists", false);
				response.put("message", "No SYSTEM_ADMIN user found");
				return ResponseEntity.ok(response);
			}
			
			// Force load company to avoid LazyInitializationException
			Company company = adminUser.getCompany();
			String companyName = company != null ? company.getName() : "null";
			Boolean companyActive = company != null ? company.getActive() : false;
			
			response.put("exists", true);
			response.put("email", adminUser.getEmail());
			response.put("active", adminUser.getActive());
			response.put("role", adminUser.getRole().name());
			response.put("companyName", companyName);
			response.put("companyActive", companyActive);
			response.put("passwordHashLength", adminUser.getPasswordHash() != null ? adminUser.getPasswordHash().length() : 0);
			
			// Test password match
			boolean passwordMatches = passwordEncoder.matches(initialAdminPassword, adminUser.getPasswordHash());
			response.put("passwordMatches", passwordMatches);
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("error", true);
			response.put("message", "Error checking admin user: " + e.getMessage());
			response.put("exception", e.getClass().getSimpleName());
			return ResponseEntity.status(500).body(response);
		}
	}

	@PostMapping("/create-admin")
	@Transactional
	public ResponseEntity<Map<String, Object>> createAdminUser() {
		Map<String, Object> response = new HashMap<>();

		// Check if SYSTEM_ADMIN user already exists
		boolean systemAdminExists = userRepository.findAll().stream()
			.anyMatch(user -> user.getRole() == Role.SYSTEM_ADMIN);

		if (systemAdminExists) {
			User existingAdmin = userRepository.findAll().stream()
				.filter(user -> user.getRole() == Role.SYSTEM_ADMIN)
				.findFirst()
				.orElse(null);
			
			response.put("success", false);
			response.put("message", "SYSTEM_ADMIN user already exists");
			if (existingAdmin != null) {
				response.put("email", existingAdmin.getEmail());
				response.put("active", existingAdmin.getActive());
			}
			return ResponseEntity.ok(response);
		}

		try {
			// Create default company for system admin
			Company defaultCompany = new Company();
			defaultCompany.setName("System Administration");
			defaultCompany.setActive(true);
			Company savedCompany = companyRepository.save(defaultCompany);

			// Create initial SYSTEM_ADMIN user
			User adminUser = new User();
			adminUser.setEmail(initialAdminEmail);
			adminUser.setPasswordHash(passwordEncoder.encode(initialAdminPassword));
			adminUser.setRole(Role.SYSTEM_ADMIN);
			adminUser.setCompany(savedCompany);
			adminUser.setActive(true);

			User savedUser = userRepository.save(adminUser);

			response.put("success", true);
			response.put("message", "Initial SYSTEM_ADMIN user created successfully");
			response.put("email", savedUser.getEmail());
			response.put("role", savedUser.getRole().name());
			response.put("password", "Use: " + initialAdminPassword);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Error creating admin user: " + e.getMessage());
			response.put("error", e.getClass().getSimpleName());
			return ResponseEntity.status(500).body(response);
		}
	}

	@PostMapping("/reset-admin-password")
	@Transactional
	public ResponseEntity<Map<String, Object>> resetAdminPassword() {
		Map<String, Object> response = new HashMap<>();
		
		User adminUser = userRepository.findAll().stream()
			.filter(user -> user.getRole() == Role.SYSTEM_ADMIN)
			.findFirst()
			.orElse(null);
		
		if (adminUser == null) {
			response.put("success", false);
			response.put("message", "No SYSTEM_ADMIN user found");
			return ResponseEntity.ok(response);
		}
		
		try {
			// Reset password
			adminUser.setPasswordHash(passwordEncoder.encode(initialAdminPassword));
			adminUser.setActive(true);
			if (adminUser.getCompany() != null) {
				adminUser.getCompany().setActive(true);
				companyRepository.save(adminUser.getCompany());
			}
			userRepository.save(adminUser);
			
			response.put("success", true);
			response.put("message", "Admin password reset successfully");
			response.put("email", adminUser.getEmail());
			response.put("password", "Use: " + initialAdminPassword);
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Error resetting password: " + e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}

	@PostMapping("/create-user")
	@Transactional
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> request) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			// Validate email
			String email = (String) request.get("email");
			if (email == null || email.isBlank()) {
				response.put("success", false);
				response.put("message", "Email is required");
				return ResponseEntity.status(400).body(response);
			}
			
			// Check if email already exists
			if (userRepository.existsByEmail(email)) {
				response.put("success", false);
				response.put("message", "User with email '" + email + "' already exists");
				return ResponseEntity.status(400).body(response);
			}
			
			// Validate password
			String password = (String) request.get("password");
			if (password == null || password.isBlank()) {
				response.put("success", false);
				response.put("message", "Password is required");
				return ResponseEntity.status(400).body(response);
			}
			
			if (password.length() < 8) {
				response.put("success", false);
				response.put("message", "Password must be at least 8 characters long");
				return ResponseEntity.status(400).body(response);
			}
			
			// Parse role
			Role role = Role.COMPANY_USER; // Default
			String roleStr = (String) request.get("role");
			if (roleStr != null && !roleStr.isBlank()) {
				try {
					role = Role.valueOf(roleStr.toUpperCase());
				} catch (IllegalArgumentException e) {
					response.put("success", false);
					response.put("message", "Invalid role. Use: SYSTEM_ADMIN, COMPANY_ADMIN, or COMPANY_USER");
					return ResponseEntity.status(400).body(response);
				}
			}
			
			// Active status
			Boolean active = true;
			if (request.containsKey("active")) {
				Object activeObj = request.get("active");
				if (activeObj instanceof Boolean) {
					active = (Boolean) activeObj;
				}
			}
			
			// Get or create default company
			Company company = companyRepository.findAll().stream()
				.findFirst()
				.orElseGet(() -> {
					Company defaultCompany = new Company();
					defaultCompany.setName("Default Company");
					defaultCompany.setActive(true);
					return companyRepository.save(defaultCompany);
				});
			
			// Create user
			User newUser = new User();
			newUser.setEmail(email);
			newUser.setPasswordHash(passwordEncoder.encode(password));
			newUser.setRole(role);
			newUser.setCompany(company);
			newUser.setActive(active);
			
			User savedUser = userRepository.save(newUser);
			
			response.put("success", true);
			response.put("message", "User created successfully");
			response.put("user", Map.of(
				"id", savedUser.getId(),
				"email", savedUser.getEmail(),
				"role", savedUser.getRole().name(),
				"active", savedUser.getActive()
			));
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Error creating user: " + e.getMessage());
			response.put("error", e.getClass().getSimpleName());
			if (e.getCause() != null) {
				response.put("cause", e.getCause().getMessage());
			}
			return ResponseEntity.status(500).body(response);
		}
	}
}

