package com.changeready.service;

import com.changeready.audit.AuditLogger;
import com.changeready.dto.company.CompanyRequest;
import com.changeready.dto.company.CompanyResponse;
import com.changeready.entity.Company;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.exception.ValidationException;
import com.changeready.repository.CompanyRepository;
import com.changeready.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepository companyRepository;
	private final AuditLogger auditLogger;

	public CompanyServiceImpl(CompanyRepository companyRepository, AuditLogger auditLogger) {
		this.companyRepository = companyRepository;
		this.auditLogger = auditLogger;
	}

	@Override
	@Transactional
	public CompanyResponse create(CompanyRequest request) {
		// Check if company name already exists
		if (companyRepository.findByName(request.getName()).isPresent()) {
			throw new ValidationException("Company with name '" + request.getName() + "' already exists");
		}

		Company company = new Company();
		company.setName(request.getName());
		company.setActive(request.getActive() != null ? request.getActive() : true);

		Company savedCompany = companyRepository.save(company);
		
		// SEC-012: Audit log company creation
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
			UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
			auditLogger.logCompanyCreated(
				principal.getId(),
				principal.getRole().name(),
				savedCompany.getId(),
				getClientIpAddress()
			);
		}
		
		return mapToResponse(savedCompany);
	}
	
	private String getClientIpAddress() {
		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				String xForwardedFor = request.getHeader("X-Forwarded-For");
				if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
					return xForwardedFor.split(",")[0].trim();
				}
				return request.getRemoteAddr();
			}
		} catch (Exception e) {
			// Ignore
		}
		return "unknown";
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompanyResponse> findAll() {
		return companyRepository.findAll().stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public CompanyResponse findById(Long id) {
		Company company = companyRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
		return mapToResponse(company);
	}

	@Override
	@Transactional
	public CompanyResponse update(Long id, CompanyRequest request) {
		Company company = companyRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

		// Check if name is being changed and if new name already exists
		if (!company.getName().equals(request.getName())) {
			if (companyRepository.findByName(request.getName()).isPresent()) {
				throw new ValidationException("Company with name '" + request.getName() + "' already exists");
			}
			company.setName(request.getName());
		}

		if (request.getActive() != null) {
			company.setActive(request.getActive());
		}

		Company updatedCompany = companyRepository.save(company);
		return mapToResponse(updatedCompany);
	}

	private CompanyResponse mapToResponse(Company company) {
		return new CompanyResponse(
			company.getId(),
			company.getName(),
			company.getActive(),
			company.getCreatedAt(),
			company.getUpdatedAt()
		);
	}
}

