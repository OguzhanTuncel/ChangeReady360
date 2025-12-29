package com.changeready.dto.reporting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Helper-Klasse für JSON-Parsing der Template-Questions
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionStructure {
	private String id;
	private String text;
	private Boolean reverse;
	private Boolean onlyPMA;
}

