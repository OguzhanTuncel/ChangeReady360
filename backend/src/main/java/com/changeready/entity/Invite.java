package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "invites", uniqueConstraints = {
	@UniqueConstraint(name = "uk_invite_token", columnNames = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_company"))
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private InviteStatus status = InviteStatus.PENDING;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "created_by")
	private Long createdBy; // ID des Users, der die Einladung erstellt hat

	@Column(name = "accepted_at")
	private LocalDateTime acceptedAt;

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

	public enum InviteStatus {
		PENDING,    // Einladung wurde erstellt, wartet auf Annahme
		ACCEPTED,   // Einladung wurde angenommen, User wurde erstellt
		EXPIRED,    // Einladung ist abgelaufen
		CANCELLED   // Einladung wurde storniert
	}
}

