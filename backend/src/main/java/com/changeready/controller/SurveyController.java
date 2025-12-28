package com.changeready.controller;

import com.changeready.dto.survey.SurveyAnswerUpdateRequest;
import com.changeready.dto.survey.SurveyInstanceCreateRequest;
import com.changeready.dto.survey.SurveyInstanceDetailResponse;
import com.changeready.dto.survey.SurveyInstanceResponse;
import com.changeready.dto.survey.SurveyTemplateResponse;
import com.changeready.security.UserPrincipal;
import com.changeready.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/surveys")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER')")
public class SurveyController {

	private final SurveyService surveyService;

	public SurveyController(SurveyService surveyService) {
		this.surveyService = surveyService;
	}

	/**
	 * GET /api/v1/surveys/templates
	 * Lädt alle aktiven Survey-Templates für die Company des Benutzers
	 */
	@GetMapping("/templates")
	public ResponseEntity<List<SurveyTemplateResponse>> getTemplates() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<SurveyTemplateResponse> templates = surveyService.getTemplates(userPrincipal);
		return ResponseEntity.ok(templates);
	}

	/**
	 * POST /api/v1/surveys/instances
	 * Erstellt eine neue Survey-Instanz
	 */
	@PostMapping("/instances")
	public ResponseEntity<SurveyInstanceResponse> createInstance(@Valid @RequestBody SurveyInstanceCreateRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		SurveyInstanceResponse instance = surveyService.createInstance(request, userPrincipal);
		return ResponseEntity.ok(instance);
	}

	/**
	 * GET /api/v1/surveys/instances
	 * Lädt alle Survey-Instanzen des aktuellen Benutzers
	 */
	@GetMapping("/instances")
	public ResponseEntity<List<SurveyInstanceResponse>> getInstances() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		List<SurveyInstanceResponse> instances = surveyService.getInstances(userPrincipal);
		return ResponseEntity.ok(instances);
	}

	/**
	 * GET /api/v1/surveys/instances/{id}
	 * Lädt eine spezifische Survey-Instanz mit Template und Antworten
	 */
	@GetMapping("/instances/{id}")
	public ResponseEntity<SurveyInstanceDetailResponse> getInstance(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		SurveyInstanceDetailResponse instance = surveyService.getInstance(id, userPrincipal);
		return ResponseEntity.ok(instance);
	}

	/**
	 * PUT /api/v1/surveys/instances/{id}/answers
	 * Speichert oder aktualisiert Antworten einer Survey-Instanz (Autosave)
	 */
	@PutMapping("/instances/{id}/answers")
	public ResponseEntity<Void> saveAnswers(
		@PathVariable Long id,
		@Valid @RequestBody SurveyAnswerUpdateRequest request
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		surveyService.saveAnswers(id, request, userPrincipal);
		return ResponseEntity.ok().build();
	}

	/**
	 * POST /api/v1/surveys/instances/{id}/submit
	 * Sendet eine Survey-Instanz ab (Status: SUBMITTED)
	 */
	@PostMapping("/instances/{id}/submit")
	public ResponseEntity<Void> submitInstance(@PathVariable Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
		
		surveyService.submitInstance(id, userPrincipal);
		return ResponseEntity.ok().build();
	}
}

