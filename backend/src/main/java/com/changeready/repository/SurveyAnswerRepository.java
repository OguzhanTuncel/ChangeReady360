package com.changeready.repository;

import com.changeready.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

	/**
	 * Findet alle Antworten einer SurveyInstance
	 * @param instanceId SurveyInstance-ID
	 * @return Liste von SurveyAnswers der Instanz
	 */
	List<SurveyAnswer> findByInstanceId(Long instanceId);

	/**
	 * Findet eine spezifische Antwort einer Instanz für eine Frage
	 * @param instanceId SurveyInstance-ID
	 * @param questionId Frage-ID (z.B. "A1.1")
	 * @return Optional SurveyAnswer
	 */
	Optional<SurveyAnswer> findByInstanceIdAndQuestionId(Long instanceId, String questionId);

	/**
	 * Prüft ob eine Antwort für eine Frage bereits existiert
	 * @param instanceId SurveyInstance-ID
	 * @param questionId Frage-ID
	 * @return true wenn Antwort existiert
	 */
	boolean existsByInstanceIdAndQuestionId(Long instanceId, String questionId);

	/**
	 * Löscht alle Antworten einer SurveyInstance (Hard Delete)
	 * @param instanceId SurveyInstance-ID
	 */
	void deleteByInstanceId(Long instanceId);
}

