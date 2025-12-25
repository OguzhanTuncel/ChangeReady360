package com.changeready.service;

import com.changeready.dto.user.UserCreateRequest;
import com.changeready.dto.user.UserUpdateRequest;
import com.changeready.dto.user.UserResponse;
import com.changeready.entity.Company;
import com.changeready.entity.Role;
import com.changeready.entity.User;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.exception.UnauthorizedException;
import com.changeready.exception.ValidationException;
import com.changeready.repository.CompanyRepository;
import com.changeready.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthService authService;

	public UserServiceImpl(
		UserRepository userRepository,
		CompanyRepository companyRepository,
		PasswordEncoder passwordEncoder,
		AuthService authService
	) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.passwordEncoder = passwordEncoder;
		this.authService = authService;
	}

	@Override
	@Transactional
	public UserResponse create(UserCreateRequest request, Long companyId) {
		// Get authenticated user to check role
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		com.changeready.security.UserPrincipal currentUser = (com.changeready.security.UserPrincipal) authentication.getPrincipal();

		// SEC-009: COMPANY_ADMIN can only create COMPANY_USER users
		if (currentUser.getRole() == Role.COMPANY_ADMIN) {
			if (request.getRole() != Role.COMPANY_USER) {
				throw new UnauthorizedException("COMPANY_ADMIN can only create COMPANY_USER users");
			}
		}

		// Validate company exists and is active
		Company company = companyRepository.findById(companyId)
			.orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

		if (!company.getActive()) {
			throw new ValidationException("Cannot create users for deactivated company");
		}

		// Ensure company isolation - COMPANY_ADMIN can only create users in their own company
		if (currentUser.getRole() == Role.COMPANY_ADMIN && !companyId.equals(currentUser.getCompanyId())) {
			throw new UnauthorizedException("Cannot create users for other companies");
		}

		// Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ValidationException("User with email '" + request.getEmail() + "' already exists");
		}

		// Validate password
		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new ValidationException("Password is required");
		}

		if (!authService.validatePassword(request.getPassword())) {
			throw new ValidationException("Password must be at least 8 characters long and contain uppercase, lowercase, and a number");
		}

		// Create user
		// Enforce role based on who is creating the user
		Role enforcedRole = request.getRole();
		if (currentUser.getRole() == Role.COMPANY_ADMIN) {
			enforcedRole = Role.COMPANY_USER; // COMPANY_ADMIN can only create COMPANY_USER
		}
		
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setRole(enforcedRole);
		user.setCompany(company);
		// SEC-009: Active status is set to true by default, not from request
		user.setActive(true);

		User savedUser = userRepository.save(user);
		return mapToResponse(savedUser);
	}

	@Override
	@Transactional
	public UserResponse createCompanyAdmin(UserCreateRequest request, Long companyId) {
		// Get authenticated user to check role - only SYSTEM_ADMIN can create COMPANY_ADMIN
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		com.changeready.security.UserPrincipal currentUser = (com.changeready.security.UserPrincipal) authentication.getPrincipal();

		if (currentUser.getRole() != Role.SYSTEM_ADMIN) {
			throw new UnauthorizedException("Only SYSTEM_ADMIN can create COMPANY_ADMIN users");
		}

		// Validate company exists and is active
		Company company = companyRepository.findById(companyId)
			.orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

		if (!company.getActive()) {
			throw new ValidationException("Cannot create users for deactivated company");
		}

		// Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ValidationException("User with email '" + request.getEmail() + "' already exists");
		}

		// Validate password - required for COMPANY_ADMIN creation
		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new ValidationException("Password is required");
		}

		if (!authService.validatePassword(request.getPassword())) {
			throw new ValidationException("Password must be at least 8 characters long and contain uppercase, lowercase, and a number");
		}

		// Force role to COMPANY_ADMIN regardless of request
		// This endpoint only creates COMPANY_ADMIN users
		
		// Create COMPANY_ADMIN user
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.COMPANY_ADMIN); // Role is enforced, not taken from request
		user.setCompany(company);
		user.setActive(request.getActive() != null ? request.getActive() : true);

		User savedUser = userRepository.save(user);
		return mapToResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserResponse> findAllByCompany(Long companyId) {
		// Get authenticated user to check company isolation
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		com.changeready.security.UserPrincipal currentUser = (com.changeready.security.UserPrincipal) authentication.getPrincipal();

		// Ensure company isolation - COMPANY_ADMIN can only see users in their own company
		if (currentUser.getRole() == Role.COMPANY_ADMIN && !companyId.equals(currentUser.getCompanyId())) {
			throw new UnauthorizedException("Cannot access users from other companies");
		}

		return userRepository.findByCompanyId(companyId).stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findById(Long id, Long companyId) {
		// Get authenticated user to check company isolation
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		com.changeready.security.UserPrincipal currentUser = (com.changeready.security.UserPrincipal) authentication.getPrincipal();

		// Ensure company isolation
		if (currentUser.getRole() == Role.COMPANY_ADMIN && !companyId.equals(currentUser.getCompanyId())) {
			throw new UnauthorizedException("Cannot access users from other companies");
		}

		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

		// Verify user belongs to the specified company
		if (!user.getCompany().getId().equals(companyId)) {
			throw new UnauthorizedException("User does not belong to the specified company");
		}

		return mapToResponse(user);
	}

	@Override
	@Transactional
	public UserResponse update(Long id, UserUpdateRequest request, Long companyId) {
		// Get authenticated user to check role
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		com.changeready.security.UserPrincipal currentUser = (com.changeready.security.UserPrincipal) authentication.getPrincipal();

		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

		// Verify user belongs to the specified company
		if (!user.getCompany().getId().equals(companyId)) {
			throw new UnauthorizedException("User does not belong to the specified company");
		}

		// Ensure company isolation
		if (currentUser.getRole() == Role.COMPANY_ADMIN && !companyId.equals(currentUser.getCompanyId())) {
			throw new UnauthorizedException("Cannot update users from other companies");
		}

		// SEC-009: UserUpdateRequest does not contain role or active fields
		// This prevents mass assignment of sensitive fields

		// Update email if provided and different
		if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
			if (userRepository.existsByEmail(request.getEmail())) {
				throw new ValidationException("User with email '" + request.getEmail() + "' already exists");
			}
			user.setEmail(request.getEmail());
		}

		// Update password if provided
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			if (!authService.validatePassword(request.getPassword())) {
				throw new ValidationException("Password must be at least 8 characters long and contain uppercase, lowercase, and a number");
			}
			user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		}

		// SEC-009: role and active status are intentionally NOT updateable
		// Separate privileged endpoints would be required for those operations

		User updatedUser = userRepository.save(user);
		return mapToResponse(updatedUser);
	}

	private UserResponse mapToResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getRole(),
			user.getCompany().getId(),
			user.getActive(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}

