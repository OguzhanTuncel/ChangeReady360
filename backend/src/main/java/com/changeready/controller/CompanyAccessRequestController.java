package com.changeready.controller;

import com.changeready.dto.companyaccessrequest.CompanyAccessRequestRequest;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestResponse;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestUpdateRequest;
import com.changeready.entity.CompanyAccessRequest;
import com.changeready.service.CompanyAccessRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-access-requests")
public class CompanyAccessRequestController {

	private final CompanyAccessRequestService service;

	public CompanyAccessRequestController(CompanyAccessRequestService service) {
		this.service = service;
	}

	/**
	 * Öffentlicher Endpoint: Jeder kann eine Zugangsanfrage stellen
	 */
	@PostMapping
	public ResponseEntity<CompanyAccessRequestResponse> createRequest(
		@Valid @RequestBody CompanyAccessRequestRequest request
	) {
		CompanyAccessRequestResponse response = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * Geschützter Endpoint: Nur SYSTEM_ADMIN kann alle Anfragen sehen
	 */
	@GetMapping
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public ResponseEntity<List<CompanyAccessRequestResponse>> getAllRequests() {
		List<CompanyAccessRequestResponse> requests = service.findAll();
		return ResponseEntity.ok(requests);
	}

	/**
	 * Geschützter Endpoint: Nur SYSTEM_ADMIN kann Anfragen nach Status filtern
	 */
	@GetMapping("/status/{status}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public ResponseEntity<List<CompanyAccessRequestResponse>> getRequestsByStatus(
		@PathVariable CompanyAccessRequest.RequestStatus status
	) {
		List<CompanyAccessRequestResponse> requests = service.findByStatus(status);
		return ResponseEntity.ok(requests);
	}

	/**
	 * Geschützter Endpoint: Nur SYSTEM_ADMIN kann eine spezifische Anfrage sehen
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public ResponseEntity<CompanyAccessRequestResponse> getRequestById(@PathVariable Long id) {
		CompanyAccessRequestResponse request = service.findById(id);
		return ResponseEntity.ok(request);
	}

	/**
	 * Geschützter Endpoint: Nur SYSTEM_ADMIN kann Anfragen bearbeiten (APPROVE/REJECT)
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public ResponseEntity<CompanyAccessRequestResponse> updateRequest(
		@PathVariable Long id,
		@Valid @RequestBody CompanyAccessRequestUpdateRequest updateRequest
	) {
		CompanyAccessRequestResponse response = service.update(id, updateRequest);
		return ResponseEntity.ok(response);
	}
}

