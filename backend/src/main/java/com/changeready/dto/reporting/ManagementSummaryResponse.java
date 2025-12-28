package com.changeready.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagementSummaryResponse {

	private Double overallReadiness; // Gesamt-Readiness in Prozent (0-100)
	private Integer readinessTrend; // Trend in Prozentpunkten (z.B. +6% oder -3%)
	private Integer stakeholderCount; // Anzahl Stakeholder
	private Integer activeMeasuresCount; // Anzahl aktiver Maßnahmen
	private LocalDate date; // Stand-Datum
}

