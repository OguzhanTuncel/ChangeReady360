package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "measures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Measure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MeasureStatus status = MeasureStatus.OPEN;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_measure_company"))
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

	public enum MeasureStatus {
		OPEN,        // Maßnahme ist offen/aktiv
		IN_PROGRESS, // Maßnahme wird bearbeitet
		COMPLETED,   // Maßnahme ist abgeschlossen
		CANCELLED    // Maßnahme wurde storniert
	}
}

