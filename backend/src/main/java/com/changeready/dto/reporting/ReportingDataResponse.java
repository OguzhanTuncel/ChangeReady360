package com.changeready.dto.reporting;

import com.changeready.dto.dashboard.TrendDataResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportingDataResponse {

	private ManagementSummaryResponse summary;
	private List<DepartmentReadinessResponse> departments;
	private TrendDataResponse trend; // Wiederverwendung von Dashboard TrendDataResponse
}

