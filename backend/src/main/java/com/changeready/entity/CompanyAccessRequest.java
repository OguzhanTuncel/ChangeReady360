package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_access_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAccessRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "contact_name", nullable = false)
	private String contactName;

	@Column(name = "contact_email", nullable = false)
	private String contactEmail;

	@Column(name = "contact_phone")
	private String contactPhone;

	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RequestStatus status = RequestStatus.PENDING;

	@Column(name = "processed_by")
	private Long processedBy; // ID des SYSTEM_ADMIN Users, der die Anfrage bearbeitet hat

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "rejection_reason", columnDefinition = "TEXT")
	private String rejectionReason;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public enum RequestStatus {
		PENDING,    // Anfrage wartet auf Bearbeitung
		APPROVED,   // Anfrage wurde genehmigt
		REJECTED    // Anfrage wurde abgelehnt
	}
}

