package com.changeready.repository;

import com.changeready.entity.CompanyAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyAccessRequestRepository extends JpaRepository<CompanyAccessRequest, Long> {
	List<CompanyAccessRequest> findByStatus(CompanyAccessRequest.RequestStatus status);
	
	List<CompanyAccessRequest> findByContactEmail(String contactEmail);
}

