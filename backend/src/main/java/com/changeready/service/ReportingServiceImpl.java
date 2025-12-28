package com.changeready.service;

import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.dto.measure.MeasureResponse;
import com.changeready.dto.reporting.DepartmentReadinessResponse;
import com.changeready.dto.reporting.ManagementSummaryResponse;
import com.changeready.dto.reporting.ReportingDataResponse;
import com.changeready.entity.SurveyAnswer;
import com.changeready.entity.SurveyInstance;
import com.changeready.repository.SurveyInstanceRepository;
import com.changeready.repository.SurveyAnswerRepository;
import com.changeready.repository.StakeholderPersonRepository;
import com.changeready.repository.StakeholderGroupRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements ReportingService {

	private final SurveyInstanceRepository surveyInstanceRepository;
	private final SurveyAnswerRepository surveyAnswerRepository;
	private final StakeholderPersonRepository stakeholderPersonRepository;
	private final StakeholderGroupRepository stakeholderGroupRepository;
	private final MeasureService measureService;
	private final ReadinessCalculationService readinessCalculationService;
	private final DashboardService dashboardService;

	public ReportingServiceImpl(
		SurveyInstanceRepository surveyInstanceRepository,
		SurveyAnswerRepository surveyAnswerRepository,
		StakeholderPersonRepository stakeholderPersonRepository,
		StakeholderGroupRepository stakeholderGroupRepository,
		MeasureService measureService,
		ReadinessCalculationService readinessCalculationService,
		DashboardService dashboardService
	) {
		this.surveyInstanceRepository = surveyInstanceRepository;
		this.surveyAnswerRepository = surveyAnswerRepository;
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
		
		double overallReadiness = 0.0;
		if (!submittedInstances.isEmpty()) {
			List<SurveyAnswer> allAnswers = new ArrayList<>();
			for (SurveyInstance instance : submittedInstances) {
				List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
				allAnswers.addAll(answers);
			}
			
			if (!allAnswers.isEmpty()) {
				overallReadiness = readinessCalculationService.calculateReadiness(allAnswers);
			}
		}
		response.setOverallReadiness(overallReadiness);
		
		// Readiness Trend: Vergleich aktueller Wert mit Wert vor 30 Tagen
		double previousReadiness = calculateReadiness30DaysAgo(companyId);
		int trend = readinessCalculationService.calculateTrend(overallReadiness, previousReadiness);
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
				double readiness = readinessCalculationService.calculateReadiness(allAnswers);
				String color = getReadinessColor(readiness);
				
				DepartmentReadinessResponse response = new DepartmentReadinessResponse();
				response.setId(department.name()); // Department name als ID (z.B. "EINKAUF")
				response.setName(department.getDisplayName());
				response.setReadiness(readiness);
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

	@Override
	public TrendDataResponse getTrendData(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Trend-Daten aus historischen Survey-Ergebnissen
		// Aktuell: Leere Liste zurückgeben
		TrendDataResponse response = new TrendDataResponse();
		response.setDataPoints(new ArrayList<>());
		response.setInsight(null);
		return response;
	}
}

