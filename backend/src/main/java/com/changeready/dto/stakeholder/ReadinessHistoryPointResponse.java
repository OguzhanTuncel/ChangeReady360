package com.changeready.dto.stakeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessHistoryPointResponse {

	private LocalDate date; // YYYY-MM-DD
	private Double readiness; // 0-100%
}

