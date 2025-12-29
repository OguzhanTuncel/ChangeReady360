package com.changeready.service;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.measure.MeasureResponse;
import com.changeready.dto.reporting.CategoryStructure;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.dto.reporting.SurveyResultResponse;
import com.changeready.dto.reporting.TemplateDepartmentResultResponse;
import com.changeready.entity.SurveyAnswer;
import com.changeready.entity.SurveyInstance;
import com.changeready.entity.SurveyTemplate;
import com.changeready.repository.SurveyInstanceRepository;
import com.changeready.repository.SurveyAnswerRepository;
import com.changeready.repository.SurveyTemplateRepository;
import com.changeready.repository.StakeholderPersonRepository;
import com.changeready.repository.StakeholderGroupRepository;
import com.changeready.security.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

	private final SurveyInstanceRepository surveyInstanceRepository;
	private final SurveyAnswerRepository surveyAnswerRepository;
	private final SurveyTemplateRepository surveyTemplateRepository;
	private final StakeholderPersonRepository stakeholderPersonRepository;
	private final StakeholderGroupRepository stakeholderGroupRepository;
	private final MeasureService measureService;
	private final ReadinessCalculationService readinessCalculationService;
	private final DashboardService dashboardService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public ReportingServiceImpl(
		SurveyInstanceRepository surveyInstanceRepository,
		SurveyAnswerRepository surveyAnswerRepository,
		SurveyTemplateRepository surveyTemplateRepository,
		StakeholderPersonRepository stakeholderPersonRepository,
		StakeholderGroupRepository stakeholderGroupRepository,
		MeasureService measureService,
		ReadinessCalculationService readinessCalculationService,
		DashboardService dashboardService
	) {
		this.surveyInstanceRepository = surveyInstanceRepository;
		this.surveyAnswerRepository = surveyAnswerRepository;
		this.surveyTemplateRepository = surveyTemplateRepository;
		this.stakeholderPersonRepository = stakeholderPersonRepository;
		this.stakeholderGroupRepository = stakeholderGroupRepository;
		this.measureService = measureService;
		this.readinessCalculationService = readinessCalculationService;
		this.dashboardService = dashboardService;
	}

	@Override
	public ReportingDataResponse getReportingData(UserPrincipal userPrincipal) {
		ReportingDataResponse response = new ReportingDataResponse();
		response.setSummary(getManagementSummary(userPrincipal));
		response.setDepartments(getDepartmentReadiness(userPrincipal));
		response.setTrend(getTrendData(userPrincipal));
		return response;
	}

	@Override
	public ManagementSummaryResponse getManagementSummary(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		ManagementSummaryResponse response = new ManagementSummaryResponse();
		
		// Overall Readiness: Berechne aus allen SUBMITTED Survey-Instanzen
		List<SurveyInstance> submittedInstances = surveyInstanceRepository
			.findByCompanyIdAndStatus(companyId, SurveyInstance.SurveyInstanceStatus.SUBMITTED);
		
		double overallReadinessRaw = 0.0;
		if (!submittedInstances.isEmpty()) {
			List<SurveyAnswer> allAnswers = new ArrayList<>();
			for (SurveyInstance instance : submittedInstances) {
				List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
				allAnswers.addAll(answers);
			}
			
			if (!allAnswers.isEmpty()) {
				overallReadinessRaw = readinessCalculationService.calculateReadiness(allAnswers);
			}
		}
		response.setOverallReadiness(roundPercent0(overallReadinessRaw));
		
		// Readiness Trend: Vergleich aktueller Wert mit Wert vor 30 Tagen
		double previousReadiness = calculateReadiness30DaysAgo(companyId);
		int trend = readinessCalculationService.calculateTrend(overallReadinessRaw, previousReadiness);
		response.setReadinessTrend(trend);
		
		// Stakeholder Count: Anzahl aller Stakeholder-Personen
		int stakeholderCount = 0;
		List<com.changeready.entity.StakeholderGroup> groups = stakeholderGroupRepository.findByCompanyId(companyId);
		for (com.changeready.entity.StakeholderGroup group : groups) {
			stakeholderCount += stakeholderPersonRepository.findByGroupId(group.getId()).size();
		}
		response.setStakeholderCount(stakeholderCount);
		
		// Active Measures Count
		List<MeasureResponse> activeMeasures = measureService.getActiveMeasures(userPrincipal);
		response.setActiveMeasuresCount(activeMeasures.size());
		
		response.setDate(LocalDate.now());
		return response;
	}

	/**
	 * Berechnet Readiness-Wert von vor 30 Tagen
	 */
	private double calculateReadiness30DaysAgo(Long companyId) {
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		
		List<SurveyInstance> oldInstances = surveyInstanceRepository.findByCompanyId(companyId)
			.stream()
			.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
			.filter(instance -> instance.getSubmittedAt() != null)
			.filter(instance -> instance.getSubmittedAt().isBefore(thirtyDaysAgo))
			.collect(Collectors.toList());
		
		if (oldInstances.isEmpty()) {
			return 0.0;
		}
		
		List<SurveyAnswer> allAnswers = new ArrayList<>();
		for (SurveyInstance instance : oldInstances) {
			List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
			allAnswers.addAll(answers);
		}
		
		if (allAnswers.isEmpty()) {
			return 0.0;
		}
		
		return readinessCalculationService.calculateReadiness(allAnswers);
	}

	@Override
	public List<DepartmentReadinessResponse> getDepartmentReadiness(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		// Lade alle SUBMITTED Survey-Instanzen der Company
		List<SurveyInstance> submittedInstances = surveyInstanceRepository
			.findByCompanyIdAndStatus(companyId, SurveyInstance.SurveyInstanceStatus.SUBMITTED);
		
		if (submittedInstances.isEmpty()) {
			return new ArrayList<>();
		}
		
		// Gruppiere nach Department
		Map<com.changeready.entity.Department, List<SurveyInstance>> instancesByDepartment = submittedInstances.stream()
			.collect(Collectors.groupingBy(SurveyInstance::getDepartment));
		
		List<DepartmentReadinessResponse> departmentReadiness = new ArrayList<>();
		
		for (Map.Entry<com.changeready.entity.Department, List<SurveyInstance>> entry : instancesByDepartment.entrySet()) {
			com.changeready.entity.Department department = entry.getKey();
			List<SurveyInstance> instances = entry.getValue();
			
			// Sammle alle Antworten dieser Abteilung
			List<SurveyAnswer> allAnswers = new ArrayList<>();
			for (SurveyInstance instance : instances) {
				List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
				allAnswers.addAll(answers);
			}
			
			// Berechne Readiness für diese Abteilung
			if (!allAnswers.isEmpty()) {
				double readinessRaw = readinessCalculationService.calculateReadiness(allAnswers);
				String color = getReadinessColor(readinessRaw);
				
				DepartmentReadinessResponse response = new DepartmentReadinessResponse();
				response.setId(department.name()); // Department name als ID (z.B. "EINKAUF")
				response.setName(department.getDisplayName());
				response.setReadiness(roundPercent0(readinessRaw));
				response.setColor(color);
				
				departmentReadiness.add(response);
			}
		}
		
		return departmentReadiness;
	}

	/**
	 * Bestimmt Farbe basierend auf Readiness-Wert
	 */
	private String getReadinessColor(double readiness) {
		if (readiness >= 75.0) {
			return "#56A080"; // Grün
		} else if (readiness >= 50.0) {
			return "#DFB55E"; // Orange
		} else {
			return "#DC2626"; // Rot
		}
	}

	private double roundPercent0(double value) {
		return (double) Math.round(value);
	}

	@Override
	public TrendDataResponse getTrendData(UserPrincipal userPrincipal) {
		// Wiederverwendung der Dashboard-Logik
		return dashboardService.getTrendData(userPrincipal);
	}

	@Override
	public List<SurveyResultResponse> getTemplateResults(Long templateId, UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		// Template laden und Company-Zugehörigkeit prüfen
		SurveyTemplate template = surveyTemplateRepository.findById(templateId)
			.orElseThrow(() -> new RuntimeException("Survey template not found: " + templateId));
		
		if (template.getCompany() != null && !template.getCompany().getId().equals(companyId)) {
			throw new RuntimeException("Survey template does not belong to your company");
		}
		
		// Lade alle SUBMITTED Instanzen dieses Templates für die Company
		List<SurveyInstance> submittedInstances = surveyInstanceRepository
			.findByTemplateIdAndCompanyId(templateId, companyId)
			.stream()
			.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
			.collect(Collectors.toList());
		
		if (submittedInstances.isEmpty()) {
			return new ArrayList<>();
		}
		
		// Parse categoriesJson
		List<CategoryStructure> categories = parseCategoriesJson(template.getCategoriesJson());
		
		// Sammle alle Antworten aller Instanzen
		Map<String, List<Integer>> answersByQuestionId = new HashMap<>();
		for (SurveyInstance instance : submittedInstances) {
			List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
			for (SurveyAnswer answer : answers) {
				answersByQuestionId.computeIfAbsent(answer.getQuestionId(), k -> new ArrayList<>())
					.add(answer.getValue());
			}
		}
		
		// Berechne Results pro Category/Subcategory
		List<SurveyResultResponse> results = new ArrayList<>();
		
		for (CategoryStructure category : categories) {
			// Null-Sicherheit: Prüfe category und subcategories
			if (category == null || category.getSubcategories() == null) {
				continue;
			}
			
			for (com.changeready.dto.reporting.SubcategoryStructure subcategory : category.getSubcategories()) {
				// Null-Sicherheit: Prüfe subcategory und questions
				if (subcategory == null || subcategory.getQuestions() == null) {
					continue;
				}
				
				// Sammle Antworten für diese Subcategory
				List<Integer> subcategoryAnswers = new ArrayList<>();
				List<String> reverseItems = new ArrayList<>();
				int totalQuestions = 0;
				
				for (com.changeready.dto.reporting.QuestionStructure question : subcategory.getQuestions()) {
					// Null-Sicherheit: Prüfe question
					if (question == null) {
						continue;
					}
					
					totalQuestions++;
					if (question.getReverse() != null && question.getReverse()) {
						// Null-Sicherheit: Prüfe question.getId()
						if (question.getId() != null) {
							reverseItems.add(question.getId());
						}
					}
					
					// Null-Sicherheit: Prüfe question.getId() vor Map-Zugriff
					if (question.getId() != null) {
						List<Integer> questionAnswers = answersByQuestionId.get(question.getId());
						if (questionAnswers != null) {
							subcategoryAnswers.addAll(questionAnswers);
						}
					}
				}
				
				// Berechne Durchschnitt wenn Antworten vorhanden
				if (!subcategoryAnswers.isEmpty()) {
					double average = subcategoryAnswers.stream()
						.mapToInt(Integer::intValue)
						.average()
						.orElse(0.0);
					
					// Runde auf 2 Dezimalstellen
					average = Math.round(average * 100.0) / 100.0;
					
					SurveyResultResponse result = new SurveyResultResponse();
					// Null-Sicherheit: Verwende leeren String falls name null
					result.setCategory(category.getName() != null ? category.getName() : "");
					result.setSubcategory(subcategory.getName() != null ? subcategory.getName() : 
						(category.getName() != null ? category.getName() : ""));
					result.setAverage(average);
					result.setAnsweredCount(subcategoryAnswers.size());
					result.setTotalCount(totalQuestions * submittedInstances.size());
					result.setReverseItems(reverseItems);
					
					results.add(result);
				}
			}
		}
		
		return results;
	}

	@Override
	public List<TemplateDepartmentResultResponse> getTemplateDepartmentResults(Long templateId, UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		// Template laden und Company-Zugehörigkeit prüfen
		SurveyTemplate template = surveyTemplateRepository.findById(templateId)
			.orElseThrow(() -> new RuntimeException("Survey template not found: " + templateId));
		
		if (template.getCompany() != null && !template.getCompany().getId().equals(companyId)) {
			throw new RuntimeException("Survey template does not belong to your company");
		}
		
		// Lade alle SUBMITTED Instanzen dieses Templates für die Company
		List<SurveyInstance> submittedInstances = surveyInstanceRepository
			.findByTemplateIdAndCompanyId(templateId, companyId)
			.stream()
			.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
			.collect(Collectors.toList());
		
		if (submittedInstances.isEmpty()) {
			return new ArrayList<>();
		}
		
		// Gruppiere nach Department
		Map<com.changeready.entity.Department, List<SurveyInstance>> instancesByDepartment = submittedInstances.stream()
			.filter(instance -> instance.getDepartment() != null)
			.collect(Collectors.groupingBy(SurveyInstance::getDepartment));
		
		// Parse categoriesJson einmal für alle Departments (fehlertolerant)
		List<CategoryStructure> categories = parseCategoriesJson(template.getCategoriesJson());
		
		// Wenn keine Categories geparst werden konnten, leere Results zurückgeben
		if (categories == null || categories.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<TemplateDepartmentResultResponse> departmentResults = new ArrayList<>();
		
		for (Map.Entry<com.changeready.entity.Department, List<SurveyInstance>> entry : instancesByDepartment.entrySet()) {
			com.changeready.entity.Department department = entry.getKey();
			List<SurveyInstance> departmentInstances = entry.getValue();
			
			// Sammle alle Antworten dieses Departments
			Map<String, List<Integer>> answersByQuestionId = new HashMap<>();
			for (SurveyInstance instance : departmentInstances) {
				List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
				for (SurveyAnswer answer : answers) {
					answersByQuestionId.computeIfAbsent(answer.getQuestionId(), k -> new ArrayList<>())
						.add(answer.getValue());
				}
			}
			
			// Berechne Results pro Category/Subcategory für dieses Department
			List<SurveyResultResponse> departmentCategoryResults = new ArrayList<>();
			
			for (CategoryStructure category : categories) {
				// Null-Sicherheit: Prüfe category und subcategories
				if (category == null || category.getSubcategories() == null) {
					continue;
				}
				
				for (com.changeready.dto.reporting.SubcategoryStructure subcategory : category.getSubcategories()) {
					// Null-Sicherheit: Prüfe subcategory und questions
					if (subcategory == null || subcategory.getQuestions() == null) {
						continue;
					}
					
					List<Integer> subcategoryAnswers = new ArrayList<>();
					List<String> reverseItems = new ArrayList<>();
					int totalQuestions = 0;
					
					for (com.changeready.dto.reporting.QuestionStructure question : subcategory.getQuestions()) {
						// Null-Sicherheit: Prüfe question
						if (question == null) {
							continue;
						}
						
						totalQuestions++;
						if (question.getReverse() != null && question.getReverse()) {
							// Null-Sicherheit: Prüfe question.getId()
							if (question.getId() != null) {
								reverseItems.add(question.getId());
							}
						}
						
						// Null-Sicherheit: Prüfe question.getId() vor Map-Zugriff
						if (question.getId() != null) {
							List<Integer> questionAnswers = answersByQuestionId.get(question.getId());
							if (questionAnswers != null) {
								subcategoryAnswers.addAll(questionAnswers);
							}
						}
					}
					
					if (!subcategoryAnswers.isEmpty()) {
						double average = subcategoryAnswers.stream()
							.mapToInt(Integer::intValue)
							.average()
							.orElse(0.0);
						average = Math.round(average * 100.0) / 100.0;
						
						SurveyResultResponse result = new SurveyResultResponse();
						// Null-Sicherheit: Verwende leeren String falls name null
						result.setCategory(category.getName() != null ? category.getName() : "");
						result.setSubcategory(subcategory.getName() != null ? subcategory.getName() : 
							(category.getName() != null ? category.getName() : ""));
						result.setAverage(average);
						result.setAnsweredCount(subcategoryAnswers.size());
						result.setTotalCount(totalQuestions * departmentInstances.size());
						result.setReverseItems(reverseItems);
						
						departmentCategoryResults.add(result);
					}
				}
			}
			
			if (!departmentCategoryResults.isEmpty()) {
				TemplateDepartmentResultResponse deptResult = new TemplateDepartmentResultResponse();
				deptResult.setDepartment(department.name());
				deptResult.setDepartmentName(department.getDisplayName());
				deptResult.setParticipantCount(departmentInstances.size());
				deptResult.setResults(departmentCategoryResults);
				
				departmentResults.add(deptResult);
			}
		}
		
		return departmentResults;
	}

	/**
	 * Parst categoriesJson String zu CategoryStructure Liste
	 * Fehlertoleranz: Bei Parsing-Fehlern wird leere Liste zurückgegeben (Empty State)
	 * statt RuntimeException zu werfen, um 500-Fehler zu vermeiden
	 */
	private List<CategoryStructure> parseCategoriesJson(String categoriesJson) {
		if (categoriesJson == null || categoriesJson.isEmpty()) {
			return new ArrayList<>();
		}
		
		try {
			List<CategoryStructure> categories = objectMapper.readValue(categoriesJson, new TypeReference<List<CategoryStructure>>() {});
			// Null-Sicherheit: Stelle sicher dass keine null-Elemente zurückgegeben werden
			if (categories == null) {
				return new ArrayList<>();
			}
			return categories;
		} catch (Exception e) {
			// Fehlertoleranz: Bei Parsing-Fehlern (ungültiges JSON, Schema-Änderungen) 
			// wird leere Liste zurückgegeben statt RuntimeException
			// Logging für Debugging, aber kein 500-Fehler für User
			System.err.println("Warning: Failed to parse categoriesJson: " + e.getMessage());
			return new ArrayList<>();
		}
	}
}

