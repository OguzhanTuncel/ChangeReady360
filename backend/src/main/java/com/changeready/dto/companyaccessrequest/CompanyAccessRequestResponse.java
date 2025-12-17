package com.changeready.dto.companyaccessrequest;

import com.changeready.entity.CompanyAccessRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAccessRequestResponse {

	private Long id;
	private String companyName;
	private String contactName;
	private String contactEmail;
	private String contactPhone;
	private String message;
	private CompanyAccessRequest.RequestStatus status;
	private Long processedBy;
	private LocalDateTime processedAt;
	private String rejectionReason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

