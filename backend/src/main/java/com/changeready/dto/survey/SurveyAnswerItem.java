package com.changeready.dto.survey;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerItem {
	@NotNull(message = "Question ID is required")
	private String questionId;

	@Min(value = 1, message = "Value must be between 1 and 5")
	@Max(value = 5, message = "Value must be between 1 and 5")
	private Integer value;
}

