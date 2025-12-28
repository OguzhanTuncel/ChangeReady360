package com.changeready.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataPointResponse {

	private LocalDate date;
	private Double actualValue; // Readiness in Prozent (0-100)
	private Double targetValue; // Optional: Zielwert für Prognose
}

