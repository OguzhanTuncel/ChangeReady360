package com.changeready.dto.companyaccessrequest;

import com.changeready.entity.CompanyAccessRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAccessRequestUpdateRequest {

	@NotNull(message = "Status is required")
	private CompanyAccessRequest.RequestStatus status;

	@Size(max = 2000, message = "Rejection reason must not exceed 2000 characters")
	private String rejectionReason;
}

