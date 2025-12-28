package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stakeholder_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	/**
	 * Material Icon Name (z.B. "groups", "business", "people")
	 */
	@Column(nullable = false, length = 50)
	private String icon;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stakeholder_group_company"))
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Impact impact;

	@Column(columnDefinition = "TEXT")
	private String description;

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

	public enum Impact {
		NIEDRIG("Niedrig"),
		MITTEL("Mittel"),
		HOCH("Hoch"),
		SEHR_HOCH("Sehr hoch"),
		STRATEGISCH("Strategisch");

		private final String displayName;

		Impact(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}

