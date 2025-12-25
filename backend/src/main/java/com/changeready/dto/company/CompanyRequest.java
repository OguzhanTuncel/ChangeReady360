package com.changeready.dto.company;

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
public class CompanyRequest {

	@NotBlank(message = "Company name is required")
	@Size(max = 255, message = "Company name must not exceed 255 characters")
	private String name;

	private Boolean active = true;
}

