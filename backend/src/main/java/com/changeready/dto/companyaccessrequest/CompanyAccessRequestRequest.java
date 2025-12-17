package com.changeready.dto.companyaccessrequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAccessRequestRequest {

	@NotBlank(message = "Company name is required")
	@Size(max = 255, message = "Company name must not exceed 255 characters")
	private String companyName;

	@NotBlank(message = "Contact name is required")
	@Size(max = 255, message = "Contact name must not exceed 255 characters")
	private String contactName;

	@NotBlank(message = "Contact email is required")
	@Email(message = "Contact email must be valid")
	@Size(max = 255, message = "Contact email must not exceed 255 characters")
	private String contactEmail;

	@Size(max = 50, message = "Contact phone must not exceed 50 characters")
	private String contactPhone;

	@Size(max = 2000, message = "Message must not exceed 2000 characters")
	private String message;
}

