package com.changeready.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an existing user
 * SEC-009: Mass assignment protection - role and active cannot be updated via this DTO
 * Only email and optionally password can be updated
 * Role and active status require separate, privileged endpoints
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

	@Email(message = "Email must be valid")
	@Size(max = 255, message = "Email must not exceed 255 characters")
	private String email;

	@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
	private String password; // Optional - only if password change is requested

	// SEC-009: role and active are NOT updateable via this DTO
	// to prevent privilege escalation and unauthorized status changes
}


