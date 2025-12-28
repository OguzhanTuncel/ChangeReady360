package com.changeready.controller;

import com.changeready.dto.measure.MeasureResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.MeasureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/measures")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER')")
public class MeasureController {

	private final MeasureService measureService;

	public MeasureController(MeasureService measureService) {
		this.measureService = measureService;
	}

	@GetMapping("/open")
	public ResponseEntity<List<MeasureResponse>> getActiveMeasures() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<MeasureResponse> measures = measureService.getActiveMeasures(userPrincipal);
		return ResponseEntity.ok(measures);
	}
}

