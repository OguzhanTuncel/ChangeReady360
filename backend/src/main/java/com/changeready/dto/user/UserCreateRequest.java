package com.changeready.dto.user;

import com.changeready.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new user
 * SEC-009: Separate DTO for creation prevents mass assignment of sensitive fields
 * SEC-014: Enhanced input validation with size constraints
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 255, message = "Email must not exceed 255 characters")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
	private String password;

	@NotNull(message = "Role is required")
	private Role role;

	// Note: active and companyId are NOT part of the request
	// They are set by the service layer based on business rules
}

