package com.changeready.dto.stakeholder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderPersonCreateRequest {
	@NotBlank(message = "Name is required")
	private String name;

	private String role;

	@Email(message = "Email must be valid")
	private String email;
}

