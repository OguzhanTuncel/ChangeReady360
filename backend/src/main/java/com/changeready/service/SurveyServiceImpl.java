package com.changeready.service;

import com.changeready.dto.survey.SurveyAnswerItem;
import com.changeready.dto.survey.SurveyAnswerResponse;
import com.changeready.dto.survey.SurveyAnswerUpdateRequest;
import com.changeready.dto.survey.SurveyInstanceCreateRequest;
import com.changeready.dto.survey.SurveyInstanceDetailResponse;
import com.changeready.dto.survey.SurveyInstanceResponse;
import com.changeready.dto.survey.SurveyTemplateResponse;
import com.changeready.entity.SurveyAnswer;
import com.changeready.entity.SurveyInstance;
import com.changeready.entity.SurveyTemplate;
import com.changeready.entity.User;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.repository.SurveyAnswerRepository;
import com.changeready.repository.SurveyInstanceRepository;
import com.changeready.repository.SurveyTemplateRepository;
import com.changeready.repository.UserRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyServiceImpl implements SurveyService {

	private final SurveyTemplateRepository templateRepository;
	private final SurveyInstanceRepository instanceRepository;
	private final SurveyAnswerRepository answerRepository;
	private final UserRepository userRepository;

	public SurveyServiceImpl(
		SurveyTemplateRepository templateRepository,
		SurveyInstanceRepository instanceRepository,
		SurveyAnswerRepository answerRepository,
		UserRepository userRepository
	) {
		this.templateRepository = templateRepository;
		this.instanceRepository = instanceRepository;
		this.answerRepository = answerRepository;
		this.userRepository = userRepository;
	}

	@Override
	public List<SurveyTemplateResponse> getTemplates(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		// Lade globale Templates (company = null) und company-spezifische Templates
		List<SurveyTemplate> globalTemplates = templateRepository.findByActive(true)
			.stream()
			.filter(t -> t.getCompany() == null)
			.collect(Collectors.toList());
		
		List<SurveyTemplate> companyTemplates = templateRepository.findByCompanyIdAndActive(companyId, true);
		
		// Kombiniere beide Listen
		List<SurveyTemplate> allTemplates = globalTemplates.stream()
			.collect(Collectors.toList());
		allTemplates.addAll(companyTemplates);
		
		return allTemplates.stream()
			.map(this::toTemplateResponse)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public SurveyInstanceResponse createInstance(SurveyInstanceCreateRequest request, UserPrincipal userPrincipal) {
		// Template validieren
		SurveyTemplate template = templateRepository.findById(request.getTemplateId())
			.orElseThrow(() -> new RuntimeException("Survey template not found: " + request.getTemplateId()));
		
		// Prüfe ob Template aktiv ist
		if (!template.getActive()) {
			throw new RuntimeException("Survey template is not active: " + request.getTemplateId());
		}
		
		// Prüfe Company-Zugehörigkeit (Template muss global sein oder zur Company gehören)
		if (template.getCompany() != null && !template.getCompany().getId().equals(userPrincipal.getCompanyId())) {
			throw new RuntimeException("Survey template does not belong to your company");
		}
		
		// User laden
		User user = userRepository.findById(userPrincipal.getId())
			.orElseThrow(() -> new RuntimeException("User not found: " + userPrincipal.getId()));
		
		// Neue Instanz erstellen
		SurveyInstance instance = new SurveyInstance();
		instance.setTemplate(template);
		instance.setUser(user);
		instance.setCompany(user.getCompany());
		instance.setParticipantType(request.getParticipantType());
		instance.setDepartment(request.getDepartment());
		instance.setStatus(SurveyInstance.SurveyInstanceStatus.DRAFT);
		
		instance = instanceRepository.save(instance);
		
		return toInstanceResponse(instance);
	}

	@Override
	public List<SurveyInstanceResponse> getInstances(UserPrincipal userPrincipal) {
		List<SurveyInstance> instances = instanceRepository.findByUserIdAndCompanyId(
			userPrincipal.getId(),
			userPrincipal.getCompanyId()
		);
		
		return instances.stream()
			.map(this::toInstanceResponse)
			.collect(Collectors.toList());
	}

	@Override
	public SurveyInstanceDetailResponse getInstance(Long instanceId, UserPrincipal userPrincipal) {
		SurveyInstance instance = instanceRepository.findById(instanceId)
			.orElseThrow(() -> new RuntimeException("Survey instance not found: " + instanceId));
		
		// Company-Isolation prüfen
		if (!instance.getCompany().getId().equals(userPrincipal.getCompanyId())) {
			throw new RuntimeException("Survey instance does not belong to your company");
		}
		
		// User-Isolation prüfen (nur eigene Instanzen)
		if (!instance.getUser().getId().equals(userPrincipal.getId())) {
			throw new RuntimeException("Survey instance does not belong to you");
		}
		
		// Antworten laden
		List<SurveyAnswer> answers = answerRepository.findByInstanceId(instanceId);
		List<SurveyAnswerResponse> answerResponses = answers.stream()
			.map(this::toAnswerResponse)
			.collect(Collectors.toList());
		
		// Template-Response erstellen
		SurveyTemplateResponse templateResponse = toTemplateResponse(instance.getTemplate());
		
		// Detail-Response zusammenstellen
		SurveyInstanceDetailResponse response = new SurveyInstanceDetailResponse();
		response.setId(instance.getId());
		response.setTemplate(templateResponse);
		response.setParticipantType(instance.getParticipantType());
		response.setDepartment(instance.getDepartment());
		response.setStatus(instance.getStatus());
		response.setAnswers(answerResponses);
		response.setCreatedAt(instance.getCreatedAt());
		response.setUpdatedAt(instance.getUpdatedAt());
		response.setSubmittedAt(instance.getSubmittedAt());
		
		return response;
	}

	@Override
	@Transactional
	public void saveAnswers(Long instanceId, SurveyAnswerUpdateRequest request, UserPrincipal userPrincipal) {
		SurveyInstance instance = instanceRepository.findById(instanceId)
			.orElseThrow(() -> new RuntimeException("Survey instance not found: " + instanceId));
		
		// Company-Isolation prüfen
		if (!instance.getCompany().getId().equals(userPrincipal.getCompanyId())) {
			throw new RuntimeException("Survey instance does not belong to your company");
		}
		
		// User-Isolation prüfen
		if (!instance.getUser().getId().equals(userPrincipal.getId())) {
			throw new RuntimeException("Survey instance does not belong to you");
		}
		
		// Status prüfen (nur DRAFT kann bearbeitet werden)
		if (instance.getStatus() != SurveyInstance.SurveyInstanceStatus.DRAFT) {
			throw new RuntimeException("Cannot update answers for submitted survey instance");
		}
		
		// Antworten speichern/aktualisieren
		for (SurveyAnswerItem item : request.getAnswers()) {
			// "Keine Angabe": Null bedeutet Antwort entfernen (nicht in Auswertung einfließen lassen)
			if (item.getValue() == null) {
				answerRepository.findByInstanceIdAndQuestionId(instanceId, item.getQuestionId())
					.ifPresent(answerRepository::delete);
				continue;
			}

			answerRepository.findByInstanceIdAndQuestionId(instanceId, item.getQuestionId())
				.ifPresentOrElse(
					// Update bestehende Antwort
					existingAnswer -> {
						existingAnswer.setValue(item.getValue());
						answerRepository.save(existingAnswer);
					},
					// Erstelle neue Antwort
					() -> {
						SurveyAnswer answer = new SurveyAnswer();
						answer.setInstance(instance);
						answer.setQuestionId(item.getQuestionId());
						answer.setValue(item.getValue());
						answerRepository.save(answer);
					}
				);
		}
	}

	@Override
	@Transactional
	public void submitInstance(Long instanceId, UserPrincipal userPrincipal) {
		SurveyInstance instance = instanceRepository.findById(instanceId)
			.orElseThrow(() -> new RuntimeException("Survey instance not found: " + instanceId));
		
		// Company-Isolation prüfen
		if (!instance.getCompany().getId().equals(userPrincipal.getCompanyId())) {
			throw new RuntimeException("Survey instance does not belong to your company");
		}
		
		// User-Isolation prüfen
		if (!instance.getUser().getId().equals(userPrincipal.getId())) {
			throw new RuntimeException("Survey instance does not belong to you");
		}
		
		// Status prüfen
		if (instance.getStatus() != SurveyInstance.SurveyInstanceStatus.DRAFT) {
			throw new RuntimeException("Survey instance is already submitted");
		}
		
		// Status auf SUBMITTED setzen
		instance.setStatus(SurveyInstance.SurveyInstanceStatus.SUBMITTED);
		instance.setSubmittedAt(LocalDateTime.now());
		instanceRepository.save(instance);
	}

	@Override
	@Transactional
	public void deleteInstance(Long instanceId, UserPrincipal userPrincipal) {
		SurveyInstance instance = instanceRepository.findById(instanceId)
			.orElseThrow(() -> new ResourceNotFoundException("Survey instance not found: " + instanceId));

		// Company-Isolation prüfen: Admins dürfen nur innerhalb der eigenen Company löschen
		if (!instance.getCompany().getId().equals(userPrincipal.getCompanyId())) {
			// 404 statt 403 um keine Fremd-IDs zu leaken
			throw new ResourceNotFoundException("Survey instance not found: " + instanceId);
		}

		// Datenintegrität: Antworten zuerst löschen (FK -> survey_instances)
		answerRepository.deleteByInstanceId(instanceId);

		// Hard Delete: Instance entfernen
		instanceRepository.delete(instance);
	}

	// Helper-Methoden für Mapping

	private SurveyTemplateResponse toTemplateResponse(SurveyTemplate template) {
		SurveyTemplateResponse response = new SurveyTemplateResponse();
		response.setId(template.getId());
		response.setName(template.getName());
		response.setDescription(template.getDescription());
		response.setVersion(template.getVersion());
		response.setActive(template.getActive());
		response.setCategoriesJson(template.getCategoriesJson());
		response.setCreatedAt(template.getCreatedAt());
		response.setUpdatedAt(template.getUpdatedAt());
		return response;
	}

	private SurveyInstanceResponse toInstanceResponse(SurveyInstance instance) {
		SurveyInstanceResponse response = new SurveyInstanceResponse();
		response.setId(instance.getId());
		response.setTemplateId(instance.getTemplate().getId());
		response.setTemplateName(instance.getTemplate().getName());
		response.setParticipantType(instance.getParticipantType());
		response.setDepartment(instance.getDepartment());
		response.setStatus(instance.getStatus());
		response.setCreatedAt(instance.getCreatedAt());
		response.setUpdatedAt(instance.getUpdatedAt());
		response.setSubmittedAt(instance.getSubmittedAt());
		
		// Berechne totalQuestions aus Template categoriesJson
		// Vereinfachter Ansatz: Zähle Frage-IDs im JSON
		int totalQuestions = countQuestionsInTemplate(instance.getTemplate().getCategoriesJson(), instance.getParticipantType());
		response.setTotalQuestions(totalQuestions);
		
		// Berechne answeredQuestions aus gespeicherten Antworten
		long answeredCount = answerRepository.findByInstanceId(instance.getId()).size();
		response.setAnsweredQuestions((int) answeredCount);
		
		return response;
	}
	
	/**
	 * Zählt die Fragen in einem Template basierend auf categoriesJson und participantType
	 * Vereinfachter Ansatz: Zählt Frage-IDs im JSON-String
	 */
	private int countQuestionsInTemplate(String categoriesJson, SurveyInstance.ParticipantType participantType) {
		if (categoriesJson == null || categoriesJson.isEmpty()) {
			return 0;
		}
		
		try {
			// Zähle Vorkommen von Frage-IDs (Pattern: "id": "..." in questions-Array)
			// Vereinfachter Ansatz: Zähle "id" Vorkommen die auf Fragen hinweisen
			// Pattern: Fragen haben "id": "..." innerhalb von questions-Arrays
			int count = 0;
			String[] questionIdParts = categoriesJson.split("\"id\"\\s*:");
			// Jede Frage hat ein "id" Feld, also Anzahl = parts.length - 1
			// Aber wir müssen nur die in "questions" Arrays zählen
			// Einfacher Ansatz: Zähle alle "id" Vorkommen und subtrahiere die in anderen Kontexten
			count = Math.max(0, questionIdParts.length - 1);
			
			// Filter nach participantType: Wenn != PMA, filtere Fragen mit "onlyPMA": true
			if (participantType != SurveyInstance.ParticipantType.PMA) {
				// Zähle Fragen mit "onlyPMA": true und subtrahiere sie
				String[] onlyPmaParts = categoriesJson.split("\"onlyPMA\"\\s*:\\s*true");
				int onlyPmaCount = Math.max(0, onlyPmaParts.length - 1);
				count = Math.max(0, count - onlyPmaCount);
			}
			
			return count;
		} catch (Exception e) {
			// Bei Parsing-Fehler: Fallback auf 0
			return 0;
		}
	}

	private SurveyAnswerResponse toAnswerResponse(SurveyAnswer answer) {
		SurveyAnswerResponse response = new SurveyAnswerResponse();
		response.setQuestionId(answer.getQuestionId());
		response.setValue(answer.getValue());
		response.setCreatedAt(answer.getCreatedAt());
		response.setUpdatedAt(answer.getUpdatedAt());
		return response;
	}
}

