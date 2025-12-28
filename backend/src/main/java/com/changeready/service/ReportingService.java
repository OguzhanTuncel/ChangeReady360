package com.changeready.service;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.security.UserPrincipal;

import java.util.List;

public interface ReportingService {

	/**
	 * Lädt alle Reporting-Daten (Summary, Departments, Trend)
	 */
	ReportingDataResponse getReportingData(UserPrincipal userPrincipal);

	/**
	 * Lädt Management Summary
	 */
	ManagementSummaryResponse getManagementSummary(UserPrincipal userPrincipal);

	/**
	 * Lädt Abteilungs-Readiness-Daten
	 */
	List<DepartmentReadinessResponse> getDepartmentReadiness(UserPrincipal userPrincipal);

	/**
	 * Lädt Trend-Daten für Chart
	 */
	TrendDataResponse getTrendData(UserPrincipal userPrincipal);
}

