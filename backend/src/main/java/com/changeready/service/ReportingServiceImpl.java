package com.changeready.service;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportingServiceImpl implements ReportingService {

	@Override
	public ReportingDataResponse getReportingData(UserPrincipal userPrincipal) {
		ReportingDataResponse response = new ReportingDataResponse();
		response.setSummary(getManagementSummary(userPrincipal));
		response.setDepartments(getDepartmentReadiness(userPrincipal));
		response.setTrend(getTrendData(userPrincipal));
		return response;
	}

	@Override
	public ManagementSummaryResponse getManagementSummary(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Datenaggregation aus Survey/Stakeholder Entities
		// Aktuell: Platzhalter-Daten zurückgeben
		ManagementSummaryResponse response = new ManagementSummaryResponse();
		response.setOverallReadiness(0.0);
		response.setReadinessTrend(0);
		response.setStakeholderCount(0);
		response.setActiveMeasuresCount(0);
		response.setDate(LocalDate.now());
		return response;
	}

	@Override
	public List<DepartmentReadinessResponse> getDepartmentReadiness(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Daten aus Survey-Ergebnissen nach Abteilungen
		// Aktuell: Leere Liste zurückgeben
		return new ArrayList<>();
	}

	@Override
	public TrendDataResponse getTrendData(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Trend-Daten aus historischen Survey-Ergebnissen
		// Aktuell: Leere Liste zurückgeben
		TrendDataResponse response = new TrendDataResponse();
		response.setDataPoints(new ArrayList<>());
		response.setInsight(null);
		return response;
	}
}

