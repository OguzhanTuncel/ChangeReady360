package com.changeready.controller;

import com.changeready.dto.stakeholder.StakeholderGroupDetailResponse;
import com.changeready.dto.stakeholder.StakeholderGroupResponse;
import com.changeready.dto.stakeholder.StakeholderKpisResponse;
import com.changeready.dto.stakeholder.StakeholderPersonResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.StakeholderService;
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
@RequestMapping("/api/v1/stakeholder")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER')")
public class StakeholderController {

	private final StakeholderService stakeholderService;

	public StakeholderController(StakeholderService stakeholderService) {
		this.stakeholderService = stakeholderService;
	}

	@GetMapping("/groups")
	public ResponseEntity<List<StakeholderGroupResponse>> getGroups() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<StakeholderGroupResponse> groups = stakeholderService.getGroups(userPrincipal);
		return ResponseEntity.ok(groups);
	}

	@GetMapping("/kpis")
	public ResponseEntity<StakeholderKpisResponse> getKpis() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		StakeholderKpisResponse kpis = stakeholderService.getKpis(userPrincipal);
		return ResponseEntity.ok(kpis);
	}

	@GetMapping("/groups/{id}")
	public ResponseEntity<StakeholderGroupDetailResponse> getGroupDetail(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		StakeholderGroupDetailResponse detail = stakeholderService.getGroupDetail(id, userPrincipal);
		return ResponseEntity.ok(detail);
	}

	@GetMapping("/groups/{id}/persons")
	public ResponseEntity<List<StakeholderPersonResponse>> getGroupPersons(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<StakeholderPersonResponse> persons = stakeholderService.getGroupPersons(id, userPrincipal);
		return ResponseEntity.ok(persons);
	}
}

