package com.changeready.service;

import com.changeready.dto.companyaccessrequest.CompanyAccessRequestRequest;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestResponse;
import com.changeready.dto.companyaccessrequest.CompanyAccessRequestUpdateRequest;
import com.changeready.entity.CompanyAccessRequest;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.exception.ValidationException;
import com.changeready.repository.CompanyAccessRequestRepository;
import com.changeready.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyAccessRequestServiceTest {

	@Mock
	private CompanyAccessRequestRepository repository;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private CompanyAccessRequestServiceImpl service;

	private UserPrincipal mockUserPrincipal;

	@BeforeEach
	void setUp() {
		mockUserPrincipal = new UserPrincipal(
			1L,
			"admin@test.com",
			"password",
			com.changeready.entity.Role.SYSTEM_ADMIN,
			1L,
			true,
			null
		);
	}

	@Test
	void create_ShouldSaveAndReturnResponse() {
		// Given
		CompanyAccessRequestRequest request = new CompanyAccessRequestRequest();
		request.setCompanyName("Test GmbH");
		request.setContactName("Max Mustermann");
		request.setContactEmail("max@test.de");
		request.setContactPhone("+49 123 456789");
		request.setMessage("Test message");

		CompanyAccessRequest savedEntity = new CompanyAccessRequest();
		savedEntity.setId(1L);
		savedEntity.setCompanyName("Test GmbH");
		savedEntity.setStatus(CompanyAccessRequest.RequestStatus.PENDING);

		when(repository.save(any(CompanyAccessRequest.class))).thenReturn(savedEntity);

		// When
		CompanyAccessRequestResponse response = service.create(request);

		// Then
		assertNotNull(response);
		assertEquals("Test GmbH", response.getCompanyName());
		assertEquals(CompanyAccessRequest.RequestStatus.PENDING, response.getStatus());
		verify(repository, times(1)).save(any(CompanyAccessRequest.class));
	}

	@Test
	void findById_WhenExists_ShouldReturnResponse() {
		// Given
		Long id = 1L;
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setId(id);
		entity.setCompanyName("Test GmbH");

		when(repository.findById(id)).thenReturn(Optional.of(entity));

		// When
		CompanyAccessRequestResponse response = service.findById(id);

		// Then
		assertNotNull(response);
		assertEquals(id, response.getId());
		assertEquals("Test GmbH", response.getCompanyName());
	}

	@Test
	void findById_WhenNotExists_ShouldThrowException() {
		// Given
		Long id = 999L;
		when(repository.findById(id)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
	}

	@Test
	void update_WhenRejectingWithoutReason_ShouldThrowValidationException() {
		// Given
		Long id = 1L;
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setId(id);
		entity.setStatus(CompanyAccessRequest.RequestStatus.PENDING);

		CompanyAccessRequestUpdateRequest updateRequest = new CompanyAccessRequestUpdateRequest();
		updateRequest.setStatus(CompanyAccessRequest.RequestStatus.REJECTED);
		updateRequest.setRejectionReason(null); // Missing!

		when(repository.findById(id)).thenReturn(Optional.of(entity));

		// When & Then
		assertThrows(ValidationException.class, () -> service.update(id, updateRequest));
		verify(repository, never()).save(any());
	}

	@Test
	void update_WhenRejectingWithReason_ShouldUpdateSuccessfully() {
		// Given
		Long id = 1L;
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setId(id);
		entity.setStatus(CompanyAccessRequest.RequestStatus.PENDING);

		CompanyAccessRequestUpdateRequest updateRequest = new CompanyAccessRequestUpdateRequest();
		updateRequest.setStatus(CompanyAccessRequest.RequestStatus.REJECTED);
		updateRequest.setRejectionReason("Test rejection reason");

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(mockUserPrincipal);
		when(repository.findById(id)).thenReturn(Optional.of(entity));
		when(repository.save(any(CompanyAccessRequest.class))).thenReturn(entity);

		// When
		CompanyAccessRequestResponse response = service.update(id, updateRequest);

		// Then
		assertNotNull(response);
		assertEquals(CompanyAccessRequest.RequestStatus.REJECTED, entity.getStatus());
		assertEquals("Test rejection reason", entity.getRejectionReason());
		assertNotNull(entity.getProcessedBy());
		assertNotNull(entity.getProcessedAt());
		verify(repository, times(1)).save(entity);
	}

	@Test
	void update_WhenAlreadyProcessed_ShouldThrowValidationException() {
		// Given
		Long id = 1L;
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setId(id);
		entity.setStatus(CompanyAccessRequest.RequestStatus.APPROVED); // Already processed

		CompanyAccessRequestUpdateRequest updateRequest = new CompanyAccessRequestUpdateRequest();
		updateRequest.setStatus(CompanyAccessRequest.RequestStatus.REJECTED); // Trying to change

		when(repository.findById(id)).thenReturn(Optional.of(entity));

		// When & Then
		assertThrows(ValidationException.class, () -> service.update(id, updateRequest));
		verify(repository, never()).save(any());
	}

	@Test
	void update_WhenApproving_ShouldUpdateSuccessfully() {
		// Given
		Long id = 1L;
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setId(id);
		entity.setStatus(CompanyAccessRequest.RequestStatus.PENDING);

		CompanyAccessRequestUpdateRequest updateRequest = new CompanyAccessRequestUpdateRequest();
		updateRequest.setStatus(CompanyAccessRequest.RequestStatus.APPROVED);

		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(mockUserPrincipal);
		when(repository.findById(id)).thenReturn(Optional.of(entity));
		when(repository.save(any(CompanyAccessRequest.class))).thenReturn(entity);

		// When
		CompanyAccessRequestResponse response = service.update(id, updateRequest);

		// Then
		assertNotNull(response);
		assertEquals(CompanyAccessRequest.RequestStatus.APPROVED, entity.getStatus());
		assertNull(entity.getRejectionReason());
		assertNotNull(entity.getProcessedBy());
		assertNotNull(entity.getProcessedAt());
		verify(repository, times(1)).save(entity);
	}

	@Test
	void findAll_ShouldReturnAllRequests() {
		// Given
		CompanyAccessRequest entity1 = new CompanyAccessRequest();
		entity1.setId(1L);
		CompanyAccessRequest entity2 = new CompanyAccessRequest();
		entity2.setId(2L);

		when(repository.findAll()).thenReturn(List.of(entity1, entity2));

		// When
		List<CompanyAccessRequestResponse> responses = service.findAll();

		// Then
		assertNotNull(responses);
		assertEquals(2, responses.size());
		verify(repository, times(1)).findAll();
	}

	@Test
	void findByStatus_ShouldReturnFilteredRequests() {
		// Given
		CompanyAccessRequest.RequestStatus status = CompanyAccessRequest.RequestStatus.PENDING;
		CompanyAccessRequest entity = new CompanyAccessRequest();
		entity.setId(1L);
		entity.setStatus(status);

		when(repository.findByStatus(status)).thenReturn(List.of(entity));

		// When
		List<CompanyAccessRequestResponse> responses = service.findByStatus(status);

		// Then
		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals(status, responses.get(0).getStatus());
		verify(repository, times(1)).findByStatus(status);
	}
}

