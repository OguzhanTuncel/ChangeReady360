package com.changeready.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO für Template-spezifische Department-Results
 * Enthält Results pro Department für ein spezifisches Template
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDepartmentResultResponse {

	private String department; // Department enum name (z.B. "EINKAUF")
	private String departmentName; // Display-Name (z.B. "Einkauf")
	private Integer participantCount; // Anzahl Teilnehmer dieses Departments
	private List<SurveyResultResponse> results; // Results pro Category/Subcategory
}

