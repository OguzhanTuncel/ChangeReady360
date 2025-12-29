package com.changeready.service;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.dto.reporting.SurveyResultResponse;
import com.changeready.dto.reporting.TemplateDepartmentResultResponse;
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
	 * Lädt Abteilungs-Readiness-Daten (aggregiert über alle Templates)
	 */
	List<DepartmentReadinessResponse> getDepartmentReadiness(UserPrincipal userPrincipal);

	/**
	 * Lädt Trend-Daten für Chart
	 */
	TrendDataResponse getTrendData(UserPrincipal userPrincipal);

	/**
	 * Lädt Template-spezifische Results (kategorisiert nach Category/Subcategory)
	 * @param templateId Template-ID
	 * @param userPrincipal Aktueller Benutzer
	 * @return Liste von SurveyResultResponse pro Category/Subcategory
	 */
	List<SurveyResultResponse> getTemplateResults(Long templateId, UserPrincipal userPrincipal);

	/**
	 * Lädt Template-spezifische Department-Results
	 * @param templateId Template-ID
	 * @param userPrincipal Aktueller Benutzer
	 * @return Liste von TemplateDepartmentResultResponse pro Department
	 */
	List<TemplateDepartmentResultResponse> getTemplateDepartmentResults(Long templateId, UserPrincipal userPrincipal);
}

