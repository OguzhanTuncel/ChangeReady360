package com.changeready.service;

import com.changeready.dto.dashboard.DashboardKpisResponse;
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

import java.util.ArrayList;
import java.util.List;
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
		
		// Stakeholder-Statistiken
		List<StakeholderGroup> groups = stakeholderGroupRepository.findByCompanyId(companyId);
		int totalStakeholders = 0;
		int promoters = 0;
		int neutrals = 0;
		int critics = 0;
		double totalReadiness = 0.0;
		int groupsWithReadiness = 0;
		
		for (StakeholderGroup group : groups) {
			// Personen der Gruppe laden
			List<StakeholderPerson> persons = stakeholderPersonRepository.findByGroupId(group.getId());
			totalStakeholders += persons.size();
			
			// Readiness für diese Gruppe berechnen
			double groupReadiness = calculateGroupReadiness(group, persons, companyId);
			if (groupReadiness > 0) {
				totalReadiness += groupReadiness;
				groupsWithReadiness++;
				
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
		
		// Overall Readiness = Durchschnitt aller Gruppen-Readiness-Werte
		if (groupsWithReadiness > 0) {
			response.setOverallReadiness(totalReadiness / groupsWithReadiness);
		} else {
			response.setOverallReadiness(0.0);
		}
		
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
		// TODO: Implementiere echte Trend-Daten aus historischen Survey-Ergebnissen
		// Aktuell: Leere Liste zurückgeben
		TrendDataResponse response = new TrendDataResponse();
		response.setDataPoints(new ArrayList<>());
		response.setInsight(null);
		return response;
	}
}

