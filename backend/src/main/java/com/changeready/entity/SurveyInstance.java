package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyInstance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false, foreignKey = @ForeignKey(name = "fk_survey_instance_template"))
	private SurveyTemplate template;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_survey_instance_user"))
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_survey_instance_company"))
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(name = "participant_type", nullable = false)
	private ParticipantType participantType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Department department;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SurveyInstanceStatus status = SurveyInstanceStatus.DRAFT;

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

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

	public enum ParticipantType {
		PMA,
		AFFECTED
	}

	public enum SurveyInstanceStatus {
		DRAFT,
		SUBMITTED
	}
}

