package com.changeready.service;

import com.changeready.dto.companyaccessrequest.CompanyAccessRequestRequest;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestResponse;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestUpdateRequest;

import java.util.List;

public interface CompanyAccessRequestService {
	CompanyAccessRequestResponse create(CompanyAccessRequestRequest request);
	List<CompanyAccessRequestResponse> findAll();
	List<CompanyAccessRequestResponse> findByStatus(com.changeready.entity.CompanyAccessRequest.RequestStatus status);
	CompanyAccessRequestResponse findById(Long id);
	CompanyAccessRequestResponse update(Long id, CompanyAccessRequestUpdateRequest request);
}

