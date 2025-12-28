package com.changeready.dto.survey;

import com.changeready.entity.Department;
import com.changeready.entity.SurveyInstance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyInstanceCreateRequest {
	@NotNull(message = "Template ID is required")
	private Long templateId;

	@NotNull(message = "Participant type is required")
	private SurveyInstance.ParticipantType participantType;

	@NotNull(message = "Department is required")
	private Department department;
}

