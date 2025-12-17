package com.changeready.dto.user;

import com.changeready.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	private String password; // Optional for updates

	@NotNull(message = "Role is required")
	private Role role;

	private Boolean active = true;
}

