package com.changeready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_answers", uniqueConstraints = {
	@UniqueConstraint(name = "uk_survey_answer_instance_question", columnNames = {"instance_id", "question_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "instance_id", nullable = false, foreignKey = @ForeignKey(name = "fk_survey_answer_instance"))
	private SurveyInstance instance;

	/**
	 * Frage-ID aus dem Template (z.B. "A1.1")
	 * Referenziert die Frage-ID aus der categoriesJson des SurveyTemplate
	 */
	@Column(name = "question_id", nullable = false, length = 50)
	private String questionId;

	/**
	 * Likert-Skala Wert: 1-5
	 * 1 = Stimme überhaupt nicht zu
	 * 5 = Stimme voll und ganz zu
	 */
	@Column(nullable = false)
	private Integer value;

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

