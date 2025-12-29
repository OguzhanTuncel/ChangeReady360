package com.changeready.controller;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.dto.reporting.SurveyResultResponse;
import com.changeready.dto.reporting.TemplateDepartmentResultResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reporting")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER')")
public class ReportingController {

	private final ReportingService reportingService;

	public ReportingController(ReportingService reportingService) {
		this.reportingService = reportingService;
	}

	@GetMapping("/data")
	public ResponseEntity<ReportingDataResponse> getReportingData() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		ReportingDataResponse data = reportingService.getReportingData(userPrincipal);
		return ResponseEntity.ok(data);
	}

	@GetMapping("/summary")
	public ResponseEntity<ManagementSummaryResponse> getManagementSummary() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		ManagementSummaryResponse summary = reportingService.getManagementSummary(userPrincipal);
		return ResponseEntity.ok(summary);
	}

	@GetMapping("/departments")
	public ResponseEntity<List<DepartmentReadinessResponse>> getDepartmentReadiness() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<DepartmentReadinessResponse> departments = reportingService.getDepartmentReadiness(userPrincipal);
		return ResponseEntity.ok(departments);
	}

	@GetMapping("/trends")
	public ResponseEntity<TrendDataResponse> getTrendData() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		TrendDataResponse trendData = reportingService.getTrendData(userPrincipal);
		return ResponseEntity.ok(trendData);
	}

	/**
	 * GET /api/v1/reporting/templates/{id}/results
	 * Lädt Template-spezifische Results (kategorisiert nach Category/Subcategory)
	 */
	@GetMapping("/templates/{id}/results")
	public ResponseEntity<List<SurveyResultResponse>> getTemplateResults(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<SurveyResultResponse> results = reportingService.getTemplateResults(id, userPrincipal);
		return ResponseEntity.ok(results);
	}

	/**
	 * GET /api/v1/reporting/templates/{id}/results/departments
	 * Lädt Template-spezifische Department-Results
	 */
	@GetMapping("/templates/{id}/results/departments")
	public ResponseEntity<List<TemplateDepartmentResultResponse>> getTemplateDepartmentResults(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<TemplateDepartmentResultResponse> results = reportingService.getTemplateDepartmentResults(id, userPrincipal);
		return ResponseEntity.ok(results);
	}
}

