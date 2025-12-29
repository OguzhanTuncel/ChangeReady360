package com.changeready.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataResponse {

	private List<TrendDataPointResponse> dataPoints;
	private String insight; // Optional: Trend-Insight (z.B. "Positive Entwicklung")
}


