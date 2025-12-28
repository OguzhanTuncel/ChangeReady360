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
public class SurveyTemplateResponse {
	private Long id;
	private String name;
	private String description;
	private String version;
	private Boolean active;
	/**
	 * Kategorien-Struktur als JSON-String
	 * Wird vom Frontend geparst
	 */
	private String categoriesJson;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

