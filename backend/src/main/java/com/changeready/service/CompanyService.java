package com.changeready.service;

import com.changeready.dto.company.CompanyRequest;
import com.changeready.dto.company.CompanyResponse;

import java.util.List;

public interface CompanyService {
	CompanyResponse create(CompanyRequest request);
	List<CompanyResponse> findAll();
	CompanyResponse findById(Long id);
	CompanyResponse update(Long id, CompanyRequest request);
}

