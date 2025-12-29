package com.changeready.service;

import com.changeready.dto.dashboard.DashboardKpisResponse;
import com.changeready.dto.dashboard.TrendDataPointResponse;
import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.entity.SurveyAnswer;
import com.changeready.entity.SurveyInstance;
import com.changeready.entity.StakeholderGroup;
import com.changeready.entity.StakeholderPerson;
import com.changeready.repository.SurveyInstanceRepository;
import com.changeready.repository.SurveyAnswerRepository;
import com.changeready.repository.StakeholderGroupRepository;
import com.changeready.repository.StakeholderPersonRepository;
import com.changeready.repository.UserRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

	private final SurveyInstanceRepository surveyInstanceRepository;
	private final SurveyAnswerRepository surveyAnswerRepository;
	private final StakeholderGroupRepository stakeholderGroupRepository;
	private final StakeholderPersonRepository stakeholderPersonRepository;
	private final UserRepository userRepository;
	private final MeasureService measureService;
	private final ReadinessCalculationService readinessCalculationService;

	public DashboardServiceImpl(
		SurveyInstanceRepository surveyInstanceRepository,
		SurveyAnswerRepository surveyAnswerRepository,
		StakeholderGroupRepository stakeholderGroupRepository,
		StakeholderPersonRepository stakeholderPersonRepository,
		UserRepository userRepository,
		MeasureService measureService,
		ReadinessCalculationService readinessCalculationService
	) {
		this.surveyInstanceRepository = surveyInstanceRepository;
		this.surveyAnswerRepository = surveyAnswerRepository;
		this.stakeholderGroupRepository = stakeholderGroupRepository;
		this.stakeholderPersonRepository = stakeholderPersonRepository;
		this.userRepository = userRepository;
		this.measureService = measureService;
		this.readinessCalculationService = readinessCalculationService;
	}

	@Override
	public DashboardKpisResponse getKpis(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		DashboardKpisResponse response = new DashboardKpisResponse();
		
		// Survey-Statistiken
		List<SurveyInstance> allInstances = surveyInstanceRepository.findByCompanyId(companyId);
		response.setTotalSurveys(allInstances.size());
		
		long completedSurveys = allInstances.stream()
			.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
			.count();
		response.setCompletedSurveys((int) completedSurveys);
		
		long openSurveys = allInstances.stream()
			.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.DRAFT)
			.count();
		response.setOpenSurveys((int) openSurveys);

		// Overall Readiness (Source of Truth): Aus allen SUBMITTED Survey-Instanzen der Company
		List<SurveyInstance> submittedInstances = allInstances.stream()
			.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
			.collect(Collectors.toList());
		if (!submittedInstances.isEmpty()) {
			List<SurveyAnswer> allAnswers = new ArrayList<>();
			for (SurveyInstance instance : submittedInstances) {
				allAnswers.addAll(surveyAnswerRepository.findByInstanceId(instance.getId()));
			}
			double readinessRaw = allAnswers.isEmpty() ? 0.0 : readinessCalculationService.calculateReadiness(allAnswers);
			response.setOverallReadiness(roundPercent0(readinessRaw));
		} else {
			response.setOverallReadiness(0.0);
		}
		
		// Stakeholder-Statistiken
		List<StakeholderGroup> groups = stakeholderGroupRepository.findByCompanyId(companyId);
		int totalStakeholders = 0;
		int promoters = 0;
		int neutrals = 0;
		int critics = 0;
		
		for (StakeholderGroup group : groups) {
			// Personen der Gruppe laden
			List<StakeholderPerson> persons = stakeholderPersonRepository.findByGroupId(group.getId());
			totalStakeholders += persons.size();
			
			// Readiness für diese Gruppe berechnen
			double groupReadiness = calculateGroupReadiness(group, persons, companyId);
			if (groupReadiness > 0) {
				// Kategorisierung
				String category = readinessCalculationService.calculatePromoterNeutralCritic(groupReadiness);
				if ("promoter".equals(category)) {
					promoters += persons.size();
				} else if ("neutral".equals(category)) {
					neutrals += persons.size();
				} else {
					critics += persons.size();
				}
			}
		}
		
		response.setTotalStakeholders(totalStakeholders);
		response.setPromoters(promoters);
		response.setNeutrals(neutrals);
		response.setCritics(critics);
		
		// Active Measures
		List<com.changeready.dto.measure.MeasureResponse> activeMeasures = measureService.getActiveMeasures(userPrincipal);
		response.setActiveMeasures(activeMeasures.size());
		
		return response;
	}

	/**
	 * Berechnet Readiness für eine Stakeholder-Gruppe
	 * Basierend auf Survey-Antworten von Personen, die zu dieser Gruppe gehören
	 */
	private double calculateGroupReadiness(StakeholderGroup group, List<StakeholderPerson> persons, Long companyId) {
		if (persons.isEmpty()) {
			return 0.0;
		}
		
		// Sammle alle Survey-Antworten von Personen dieser Gruppe
		List<SurveyAnswer> allAnswers = new ArrayList<>();
		
		for (StakeholderPerson person : persons) {
			// Versuche Person zu User zu mappen (via email)
			if (person.getEmail() != null && !person.getEmail().isEmpty()) {
				userRepository.findByEmail(person.getEmail())
					.ifPresent(user -> {
						// Lade alle SUBMITTED Survey-Instanzen dieses Users
						List<SurveyInstance> userInstances = surveyInstanceRepository
							.findByUserIdAndCompanyId(user.getId(), companyId)
							.stream()
							.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
							.collect(Collectors.toList());
						
						// Sammle alle Antworten dieser Instanzen
						for (SurveyInstance instance : userInstances) {
							List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
							allAnswers.addAll(answers);
						}
					});
			}
		}
		
		// Berechne Readiness aus allen gesammelten Antworten
		if (allAnswers.isEmpty()) {
			return 0.0;
		}
		
		return readinessCalculationService.calculateReadiness(allAnswers);
	}

	@Override
	public TrendDataResponse getTrendData(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		// Lade alle SUBMITTED Survey-Instanzen der Company
		List<SurveyInstance> submittedInstances = surveyInstanceRepository
			.findByCompanyIdAndStatus(companyId, SurveyInstance.SurveyInstanceStatus.SUBMITTED);
		
		if (submittedInstances.isEmpty()) {
			TrendDataResponse response = new TrendDataResponse();
			response.setDataPoints(new ArrayList<>());
			response.setInsight("Noch keine Daten verfügbar");
			return response;
		}
		
		// Gruppiere nach Datum (submittedAt) und berechne Readiness pro Tag
		Map<LocalDate, List<SurveyInstance>> instancesByDate = submittedInstances.stream()
			.filter(instance -> instance.getSubmittedAt() != null)
			.collect(Collectors.groupingBy(instance -> instance.getSubmittedAt().toLocalDate()));
		
		// Erstelle Trend-Datenpunkte
		List<TrendDataPointResponse> dataPoints = new ArrayList<>();
		
		for (Map.Entry<LocalDate, List<SurveyInstance>> entry : instancesByDate.entrySet()) {
			LocalDate date = entry.getKey();
			List<SurveyInstance> instances = entry.getValue();
			
			// Sammle alle Antworten dieser Instanzen
			List<SurveyAnswer> allAnswers = new ArrayList<>();
			for (SurveyInstance instance : instances) {
				List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
				allAnswers.addAll(answers);
			}
			
			// Berechne Readiness für diesen Tag
			if (!allAnswers.isEmpty()) {
				double readinessRaw = readinessCalculationService.calculateReadiness(allAnswers);
				double readinessRounded = roundPercent0(readinessRaw);
				
				TrendDataPointResponse point = new TrendDataPointResponse();
				point.setDate(date);
				point.setActualValue(readinessRounded);
				point.setTargetValue(null); // Target-Werte werden später hinzugefügt
				dataPoints.add(point);
			}
		}
		
		// Sortiere nach Datum
		dataPoints.sort(Comparator.comparing(TrendDataPointResponse::getDate));
		
		// Berechne Insight
		String insight = calculateInsight(dataPoints);
		
		TrendDataResponse response = new TrendDataResponse();
		response.setDataPoints(dataPoints);
		response.setInsight(insight);
		return response;
	}

	/**
	 * Berechnet Insight-Text basierend auf Trend-Daten
	 */
	private String calculateInsight(List<TrendDataPointResponse> dataPoints) {
		if (dataPoints.size() < 2) {
			return "Ausreichend Daten für Trend-Analyse vorhanden";
		}
		
		TrendDataPointResponse first = dataPoints.get(0);
		TrendDataPointResponse last = dataPoints.get(dataPoints.size() - 1);
		
		double trend = last.getActualValue() - first.getActualValue();
		
		if (trend > 5) {
			return "Positive Entwicklung - Readiness steigt kontinuierlich";
		} else if (trend > 0) {
			return "Leichte positive Entwicklung";
		} else if (trend < -5) {
			return "Aufmerksamkeit erforderlich - Readiness sinkt";
		} else if (trend < 0) {
			return "Leichte Verschlechterung";
		} else {
			return "Stabile Entwicklung";
		}
	}

	private double roundPercent0(double value) {
		return (double) Math.round(value);
	}
}

