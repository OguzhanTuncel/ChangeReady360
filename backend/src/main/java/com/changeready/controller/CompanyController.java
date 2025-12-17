package com.changeready.controller;

import com.changeready.dto.company.CompanyRequest;
import com.changeready.dto.company.CompanyResponse;
import com.changeready.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/companies")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class CompanyController {

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@PostMapping
	public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
		CompanyResponse response = companyService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
		List<CompanyResponse> companies = companyService.findAll();
		return ResponseEntity.ok(companies);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
		CompanyResponse company = companyService.findById(id);
		return ResponseEntity.ok(company);
	}

	@PutMapping("/{id}")
	public ResponseEntity<CompanyResponse> updateCompany(
		@PathVariable Long id,
		@Valid @RequestBody CompanyRequest request
	) {
		CompanyResponse response = companyService.update(id, request);
		return ResponseEntity.ok(response);
	}
}

