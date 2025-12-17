package com.changeready.service;

import com.changeready.dto.company.CompanyRequest;
import com.changeready.dto.company.CompanyResponse;
import com.changeready.entity.Company;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.exception.ValidationException;
import com.changeready.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepository companyRepository;

	public CompanyServiceImpl(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
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
		return mapToResponse(savedCompany);
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

