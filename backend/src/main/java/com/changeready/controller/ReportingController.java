package com.changeready.controller;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
}

