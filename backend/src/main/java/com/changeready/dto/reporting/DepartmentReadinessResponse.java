package com.changeready.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReadinessResponse {

	private String id; // Department enum name (z.B. "EINKAUF")
	private String name;
	private Double readiness; // Readiness in Prozent (0-100)
	private String color; // Farbcodierung für Visualisierung (z.B. "#56A080")
}

