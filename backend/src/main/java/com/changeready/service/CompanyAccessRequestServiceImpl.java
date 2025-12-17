package com.changeready.service;

import com.changeready.dto.companyaccessrequest.CompanyAccessRequestRequest;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestResponse;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestUpdateRequest;
import com.changeready.entity.CompanyAccessRequest;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.exception.ValidationException;
import com.changeready.repository.CompanyAccessRequestRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyAccessRequestServiceImpl implements CompanyAccessRequestService {

	private static final String REQUEST_NOT_FOUND_MESSAGE = "Company access request not found with id: ";
	private static final String REJECTION_REASON_REQUIRED_MESSAGE = "Rejection reason is required when rejecting a request";
	private static final String STATUS_CHANGE_NOT_ALLOWED_MESSAGE = "Cannot change status of a request that has already been processed";

	private final CompanyAccessRequestRepository repository;

	public CompanyAccessRequestServiceImpl(CompanyAccessRequestRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public CompanyAccessRequestResponse create(CompanyAccessRequestRequest request) {
		CompanyAccessRequest accessRequest = mapToEntity(request);
		CompanyAccessRequest savedRequest = repository.save(accessRequest);
		return mapToResponse(savedRequest);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompanyAccessRequestResponse> findAll() {
		return repository.findAll().stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompanyAccessRequestResponse> findByStatus(CompanyAccessRequest.RequestStatus status) {
		return repository.findByStatus(status).stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public CompanyAccessRequestResponse findById(Long id) {
		CompanyAccessRequest request = findRequestById(id);
		return mapToResponse(request);
	}

	@Override
	@Transactional
	public CompanyAccessRequestResponse update(Long id, CompanyAccessRequestUpdateRequest updateRequest) {
		CompanyAccessRequest request = findRequestById(id);
		
		validateUpdateRequest(request, updateRequest);
		applyUpdate(request, updateRequest);
		
		CompanyAccessRequest updatedRequest = repository.save(request);
		return mapToResponse(updatedRequest);
	}

	private CompanyAccessRequest findRequestById(Long id) {
		return repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(REQUEST_NOT_FOUND_MESSAGE + id));
	}

	private CompanyAccessRequest mapToEntity(CompanyAccessRequestRequest request) {
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setCompanyName(request.getCompanyName());
		entity.setContactName(request.getContactName());
		entity.setContactEmail(request.getContactEmail());
		entity.setContactPhone(request.getContactPhone());
		entity.setMessage(request.getMessage());
		entity.setStatus(CompanyAccessRequest.RequestStatus.PENDING);
		return entity;
	}

	private void validateUpdateRequest(CompanyAccessRequest request, CompanyAccessRequestUpdateRequest updateRequest) {
		validateRejectionReason(updateRequest);
		validateStatusChange(request, updateRequest);
	}

	private void validateRejectionReason(CompanyAccessRequestUpdateRequest updateRequest) {
		if (updateRequest.getStatus() == CompanyAccessRequest.RequestStatus.REJECTED
			&& isRejectionReasonMissing(updateRequest.getRejectionReason())) {
			throw new ValidationException(REJECTION_REASON_REQUIRED_MESSAGE);
		}
	}

	private boolean isRejectionReasonMissing(String rejectionReason) {
		return rejectionReason == null || rejectionReason.isBlank();
	}

	private void validateStatusChange(CompanyAccessRequest request, CompanyAccessRequestUpdateRequest updateRequest) {
		boolean isAlreadyProcessed = request.getStatus() != CompanyAccessRequest.RequestStatus.PENDING;
		boolean isStatusChanging = request.getStatus() != updateRequest.getStatus();
		
		if (isAlreadyProcessed && isStatusChanging) {
			throw new ValidationException(STATUS_CHANGE_NOT_ALLOWED_MESSAGE);
		}
	}

	private void applyUpdate(CompanyAccessRequest request, CompanyAccessRequestUpdateRequest updateRequest) {
		UserPrincipal currentUser = getCurrentUser();
		
		request.setStatus(updateRequest.getStatus());
		request.setProcessedBy(currentUser.getId());
		request.setProcessedAt(LocalDateTime.now());
		setRejectionReason(request, updateRequest);
	}

	private void setRejectionReason(CompanyAccessRequest request, CompanyAccessRequestUpdateRequest updateRequest) {
		if (updateRequest.getStatus() == CompanyAccessRequest.RequestStatus.REJECTED) {
			request.setRejectionReason(updateRequest.getRejectionReason());
		} else {
			request.setRejectionReason(null);
		}
	}

	private UserPrincipal getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (UserPrincipal) authentication.getPrincipal();
	}

	private CompanyAccessRequestResponse mapToResponse(CompanyAccessRequest request) {
		return new CompanyAccessRequestResponse(
			request.getId(),
			request.getCompanyName(),
			request.getContactName(),
			request.getContactEmail(),
			request.getContactPhone(),
			request.getMessage(),
			request.getStatus(),
			request.getProcessedBy(),
			request.getProcessedAt(),
			request.getRejectionReason(),
			request.getCreatedAt(),
			request.getUpdatedAt()
		);
	}
}

