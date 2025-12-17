package com.changeready.dto.invite;

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
public class InviteRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	@NotBlank(message = "Company name is required")
	private String companyName; // Für SYSTEM_ADMIN: Name der Company (wird erstellt falls nicht vorhanden)

	// Für COMPANY_ADMIN: companyId wird automatisch aus dem eingeloggten User genommen
}

