package com.changeready.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerResponse {
	private String questionId;
	private Integer value;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

