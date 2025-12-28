package com.changeready.dto.survey;

import com.changeready.entity.Department;
import com.changeready.entity.SurveyInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyInstanceDetailResponse {
	private Long id;
	private SurveyTemplateResponse template;
	private SurveyInstance.ParticipantType participantType;
	private Department department;
	private SurveyInstance.SurveyInstanceStatus status;
	private List<SurveyAnswerResponse> answers;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime submittedAt;
}

