package com.changeready.dto.survey;

import com.changeready.entity.Department;
import com.changeready.entity.SurveyInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyInstanceResponse {
	private Long id;
	private Long templateId;
	private String templateName;
	private SurveyInstance.ParticipantType participantType;
	private Department department;
	private SurveyInstance.SurveyInstanceStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime submittedAt;
}

