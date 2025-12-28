package com.changeready.dto.survey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerUpdateRequest {
	@NotEmpty(message = "Answers list cannot be empty")
	@Valid
	private List<SurveyAnswerItem> answers;
}

