package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stakeholder_persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderPerson {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stakeholder_person_group"))
	private StakeholderGroup group;

	@Column(nullable = false)
	private String name;

	@Column(length = 100)
	private String role;

	/**
	 * Optional: E-Mail für Mapping zu User-Entity (falls Person auch User ist)
	 */
	@Column(length = 255)
	private String email;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}

