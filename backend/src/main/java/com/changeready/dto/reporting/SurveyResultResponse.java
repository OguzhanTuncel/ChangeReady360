package com.changeready.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO für Template-spezifische Survey-Results
 * Enthält aggregierte Ergebnisse pro Category/Subcategory
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResultResponse {

	private String category; // Kategorie-Name (z.B. "Wissen", "Fähigkeit")
	private String subcategory; // Subkategorie-Name (z.B. "Kommunikation", "Schulung")
	private Double average; // Durchschnittswert (1-5 Skala)
	private Integer answeredCount; // Anzahl beantworteter Fragen
	private Integer totalCount; // Gesamtanzahl Fragen (theoretisch)
	private List<String> reverseItems; // Frage-IDs die umgekehrt gewertet werden müssen
}

