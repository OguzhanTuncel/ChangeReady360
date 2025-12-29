package com.changeready.dto.stakeholder;

import com.changeready.entity.StakeholderGroup;
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
public class StakeholderGroupCreateRequest {
	@NotBlank(message = "Name is required")
	private String name;

	private String icon;

	@NotNull(message = "Impact is required")
	private StakeholderGroup.Impact impact;

	private String description;
}

