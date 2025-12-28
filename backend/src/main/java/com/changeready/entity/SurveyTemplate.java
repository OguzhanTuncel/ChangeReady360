package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private String version = "1.0";

	@Column(nullable = false)
	private Boolean active = true;

	/**
	 * Kategorien-Struktur als JSON gespeichert
	 * Format: JSON-Array mit Kategorien, Subkategorien und Fragen
	 * Jede Frage kann ein ADKAR-Bereich zugeordnet werden
	 */
	@Column(columnDefinition = "TEXT", nullable = false)
	private String categoriesJson;

	/**
	 * Optional: Company-ID falls Template company-spezifisch ist
	 * null = globales Template für alle Companies
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", foreignKey = @ForeignKey(name = "fk_survey_template_company"))
	private Company company;

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
}
