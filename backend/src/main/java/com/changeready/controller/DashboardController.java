package com.changeready.controller;

import com.changeready.dto.dashboard.DashboardKpisResponse;
import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER')")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/kpis")
	public ResponseEntity<DashboardKpisResponse> getKpis() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		DashboardKpisResponse kpis = dashboardService.getKpis(userPrincipal);
		return ResponseEntity.ok(kpis);
	}

	@GetMapping("/trends")
	public ResponseEntity<TrendDataResponse> getTrendData() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		TrendDataResponse trendData = dashboardService.getTrendData(userPrincipal);
		return ResponseEntity.ok(trendData);
	}
}

