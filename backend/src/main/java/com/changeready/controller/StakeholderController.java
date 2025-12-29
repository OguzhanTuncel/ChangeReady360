package com.changeready.controller;

import com.changeready.dto.stakeholder.StakeholderGroupCreateRequest;
import com.changeready.dto.stakeholder.StakeholderGroupDetailResponse;
import com.changeready.dto.stakeholder.StakeholderGroupResponse;
import com.changeready.dto.stakeholder.StakeholderGroupUpdateRequest;
import com.changeready.dto.stakeholder.StakeholderKpisResponse;
import com.changeready.dto.stakeholder.StakeholderPersonCreateRequest;
import com.changeready.dto.stakeholder.StakeholderPersonResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.StakeholderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

	/**
	 * POST /api/v1/stakeholder/groups
	 * Erstellt eine neue Stakeholder-Gruppe
	 * SYSTEM_ADMIN und COMPANY_ADMIN dürfen Gruppen erstellen
	 */
	@PostMapping("/groups")
	@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN')")
	public ResponseEntity<StakeholderGroupResponse> createGroup(@Valid @RequestBody StakeholderGroupCreateRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		StakeholderGroupResponse group = stakeholderService.createGroup(request, userPrincipal);
		return ResponseEntity.ok(group);
	}

	/**
	 * PUT /api/v1/stakeholder/groups/{id}
	 * Aktualisiert eine Stakeholder-Gruppe
	 * SYSTEM_ADMIN und COMPANY_ADMIN dürfen Gruppen aktualisieren
	 */
	@PutMapping("/groups/{id}")
	@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN')")
	public ResponseEntity<StakeholderGroupResponse> updateGroup(
		@PathVariable Long id,
		@Valid @RequestBody StakeholderGroupUpdateRequest request
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		StakeholderGroupResponse group = stakeholderService.updateGroup(id, request, userPrincipal);
		return ResponseEntity.ok(group);
	}

	/**
	 * DELETE /api/v1/stakeholder/groups/{id}
	 * Löscht eine Stakeholder-Gruppe (inkl. Personen)
	 * SYSTEM_ADMIN und COMPANY_ADMIN dürfen Gruppen löschen
	 */
	@DeleteMapping("/groups/{id}")
	@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN')")
	public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		stakeholderService.deleteGroup(id, userPrincipal);
		return ResponseEntity.ok().build();
	}

	/**
	 * POST /api/v1/stakeholder/groups/{id}/persons
	 * Fügt eine Person zu einer Stakeholder-Gruppe hinzu
	 * SYSTEM_ADMIN und COMPANY_ADMIN dürfen Personen hinzufügen
	 */
	@PostMapping("/groups/{id}/persons")
	@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN')")
	public ResponseEntity<StakeholderPersonResponse> addPerson(
		@PathVariable Long id,
		@Valid @RequestBody StakeholderPersonCreateRequest request
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		StakeholderPersonResponse person = stakeholderService.addPerson(id, request, userPrincipal);
		return ResponseEntity.ok(person);
	}
}

