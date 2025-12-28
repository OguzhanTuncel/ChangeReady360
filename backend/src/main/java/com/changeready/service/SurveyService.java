package com.changeready.service;

import com.changeready.dto.survey.SurveyInstanceCreateRequest;
import com.changeready.dto.survey.SurveyInstanceDetailResponse;
import com.changeready.dto.survey.SurveyInstanceResponse;
import com.changeready.dto.survey.SurveyAnswerUpdateRequest;
import com.changeready.dto.survey.SurveyTemplateResponse;
import com.changeready.security.UserPrincipal;

import java.util.List;

public interface SurveyService {

	/**
	 * Lädt alle aktiven Survey-Templates für die Company des Benutzers
	 * @param userPrincipal Aktueller Benutzer
	 * @return Liste von aktiven Templates
	 */
	List<SurveyTemplateResponse> getTemplates(UserPrincipal userPrincipal);

	/**
	 * Erstellt eine neue Survey-Instanz
	 * @param request Create-Request mit templateId, participantType, department
	 * @param userPrincipal Aktueller Benutzer
	 * @return Erstellte SurveyInstance
	 */
	SurveyInstanceResponse createInstance(SurveyInstanceCreateRequest request, UserPrincipal userPrincipal);

	/**
	 * Lädt alle Survey-Instanzen des aktuellen Benutzers
	 * @param userPrincipal Aktueller Benutzer
	 * @return Liste von SurveyInstances des Users
	 */
	List<SurveyInstanceResponse> getInstances(UserPrincipal userPrincipal);

	/**
	 * Lädt eine spezifische Survey-Instanz mit Template und Antworten
	 * @param instanceId Instanz-ID
	 * @param userPrincipal Aktueller Benutzer
	 * @return SurveyInstanceDetailResponse mit Template und Antworten
	 */
	SurveyInstanceDetailResponse getInstance(Long instanceId, UserPrincipal userPrincipal);

	/**
	 * Speichert oder aktualisiert Antworten einer Survey-Instanz (Autosave)
	 * @param instanceId Instanz-ID
	 * @param request Antwort-Updates
	 * @param userPrincipal Aktueller Benutzer
	 */
	void saveAnswers(Long instanceId, SurveyAnswerUpdateRequest request, UserPrincipal userPrincipal);

	/**
	 * Sendet eine Survey-Instanz ab (Status: SUBMITTED)
	 * @param instanceId Instanz-ID
	 * @param userPrincipal Aktueller Benutzer
	 */
	void submitInstance(Long instanceId, UserPrincipal userPrincipal);
}

