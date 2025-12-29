package com.changeready.service;

import com.changeready.dto.stakeholder.StakeholderGroupCreateRequest;
import com.changeready.dto.stakeholder.StakeholderGroupDetailResponse;
import com.changeready.dto.stakeholder.StakeholderGroupResponse;
import com.changeready.dto.stakeholder.StakeholderGroupUpdateRequest;
import com.changeready.dto.stakeholder.StakeholderKpisResponse;
import com.changeready.dto.stakeholder.StakeholderPersonCreateRequest;
import com.changeready.dto.stakeholder.StakeholderPersonResponse;
import com.changeready.entity.Company;
import com.changeready.entity.Department;
import com.changeready.entity.SurveyAnswer;
import com.changeready.entity.SurveyInstance;
import com.changeready.entity.StakeholderGroup;
import com.changeready.entity.StakeholderPerson;
import com.changeready.repository.CompanyRepository;
import com.changeready.repository.SurveyAnswerRepository;
import com.changeready.repository.SurveyInstanceRepository;
import com.changeready.repository.StakeholderGroupRepository;
import com.changeready.repository.StakeholderPersonRepository;
import com.changeready.repository.UserRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StakeholderServiceImpl implements StakeholderService {

	private final StakeholderGroupRepository groupRepository;
	private final StakeholderPersonRepository personRepository;
	private final CompanyRepository companyRepository;
	private final SurveyInstanceRepository surveyInstanceRepository;
	private final SurveyAnswerRepository surveyAnswerRepository;
	private final UserRepository userRepository;
	private final ReadinessCalculationService readinessCalculationService;

	public StakeholderServiceImpl(
		StakeholderGroupRepository groupRepository,
		StakeholderPersonRepository personRepository,
		CompanyRepository companyRepository,
		SurveyInstanceRepository surveyInstanceRepository,
		SurveyAnswerRepository surveyAnswerRepository,
		UserRepository userRepository,
		ReadinessCalculationService readinessCalculationService
	) {
		this.groupRepository = groupRepository;
		this.personRepository = personRepository;
		this.companyRepository = companyRepository;
		this.surveyInstanceRepository = surveyInstanceRepository;
		this.surveyAnswerRepository = surveyAnswerRepository;
		this.userRepository = userRepository;
		this.readinessCalculationService = readinessCalculationService;
	}

	@Override
	public List<StakeholderGroupResponse> getGroups(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		List<StakeholderGroup> groups = groupRepository.findByCompanyId(companyId);
		
		return groups.stream()
			.map(group -> {
				List<StakeholderPerson> persons = personRepository.findByGroupId(group.getId());
				int participantCount = persons.size();

				// Optional: Department-Mapping über Gruppenname (z.B. "Einkauf" -> EINKAUF)
				Optional<Department> mappedDepartment = resolveDepartmentFromGroupName(group.getName());
				if (mappedDepartment.isPresent()) {
					List<SurveyInstance> deptInstances = getSubmittedInstancesForDepartment(companyId, mappedDepartment.get(), null, null);
					participantCount = deptInstances.size();
				}
				
				// Berechne Readiness für diese Gruppe
				double readinessRaw = mappedDepartment.isPresent()
					? calculateDepartmentReadiness(companyId, mappedDepartment.get(), null, null)
					: calculateGroupReadiness(group, persons, companyId);
				
				// Berechne Promoter/Neutral/Critics
				String category = readinessCalculationService.calculatePromoterNeutralCritic(readinessRaw);
				int promoters = 0;
				int neutrals = 0;
				int critics = 0;
				
				if ("promoter".equals(category)) {
					promoters = participantCount;
				} else if ("neutral".equals(category)) {
					neutrals = participantCount;
				} else {
					critics = participantCount;
				}
				
				// Berechne Trend (aktueller Wert vs. Wert vor 30 Tagen)
				double previousReadinessRaw = mappedDepartment.isPresent()
					? calculateDepartmentReadiness(companyId, mappedDepartment.get(), null, LocalDateTime.now().minusDays(30))
					: calculateGroupReadiness30DaysAgo(group, persons, companyId);
				int trend = readinessCalculationService.calculateTrend(readinessRaw, previousReadinessRaw);
				
				// Berechne Status
				String status = readinessCalculationService.calculateStatus(readinessRaw);
				
				StakeholderGroupResponse response = toGroupResponse(group);
				response.setParticipantCount(participantCount);
				response.setReadiness(roundPercent0(readinessRaw));
				response.setTrend(trend);
				response.setPromoters(promoters);
				response.setNeutrals(neutrals);
				response.setCritics(critics);
				response.setStatus(status);
				
				return response;
			})
			.collect(Collectors.toList());
	}

	/**
	 * Berechnet Readiness für eine Stakeholder-Gruppe
	 */
	private double calculateGroupReadiness(StakeholderGroup group, List<StakeholderPerson> persons, Long companyId) {
		if (persons.isEmpty()) {
			return 0.0;
		}
		
		List<SurveyAnswer> allAnswers = new ArrayList<>();
		
		for (StakeholderPerson person : persons) {
			if (person.getEmail() != null && !person.getEmail().isEmpty()) {
				userRepository.findByEmail(person.getEmail())
					.ifPresent(user -> {
						List<SurveyInstance> userInstances = surveyInstanceRepository
							.findByUserIdAndCompanyId(user.getId(), companyId)
							.stream()
							.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
							.collect(Collectors.toList());
						
						for (SurveyInstance instance : userInstances) {
							List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
							allAnswers.addAll(answers);
						}
					});
			}
		}
		
		if (allAnswers.isEmpty()) {
			return 0.0;
		}
		
		return readinessCalculationService.calculateReadiness(allAnswers);
	}

	/**
	 * Berechnet Readiness für eine Gruppe von vor 30 Tagen
	 */
	private double calculateGroupReadiness30DaysAgo(StakeholderGroup group, List<StakeholderPerson> persons, Long companyId) {
		if (persons.isEmpty()) {
			return 0.0;
		}
		
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		List<SurveyAnswer> allAnswers = new ArrayList<>();
		
		for (StakeholderPerson person : persons) {
			if (person.getEmail() != null && !person.getEmail().isEmpty()) {
				userRepository.findByEmail(person.getEmail())
					.ifPresent(user -> {
						List<SurveyInstance> userInstances = surveyInstanceRepository
							.findByUserIdAndCompanyId(user.getId(), companyId)
							.stream()
							.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
							.filter(instance -> instance.getSubmittedAt() != null)
							.filter(instance -> instance.getSubmittedAt().isBefore(thirtyDaysAgo))
							.collect(Collectors.toList());
						
						for (SurveyInstance instance : userInstances) {
							List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
							allAnswers.addAll(answers);
						}
					});
			}
		}
		
		if (allAnswers.isEmpty()) {
			return 0.0;
		}
		
		return readinessCalculationService.calculateReadiness(allAnswers);
	}

	@Override
	public StakeholderKpisResponse getKpis(UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		List<StakeholderGroup> groups = groupRepository.findByCompanyId(companyId);
		
		int total = 0;
		int promoters = 0;
		int neutrals = 0;
		int critics = 0;
		
		for (StakeholderGroup group : groups) {
			List<StakeholderPerson> persons = personRepository.findByGroupId(group.getId());
			int participantCount = persons.size();

			Optional<Department> mappedDepartment = resolveDepartmentFromGroupName(group.getName());
			if (mappedDepartment.isPresent()) {
				participantCount = getSubmittedInstancesForDepartment(companyId, mappedDepartment.get(), null, null).size();
			}
			total += participantCount;
			
			// Berechne Readiness für diese Gruppe
			double readinessRaw = mappedDepartment.isPresent()
				? calculateDepartmentReadiness(companyId, mappedDepartment.get(), null, null)
				: calculateGroupReadiness(group, persons, companyId);
			
			// Kategorisiere basierend auf Readiness
			String category = readinessCalculationService.calculatePromoterNeutralCritic(readinessRaw);
			if ("promoter".equals(category)) {
				promoters += participantCount;
			} else if ("neutral".equals(category)) {
				neutrals += participantCount;
			} else {
				critics += participantCount;
			}
		}
		
		StakeholderKpisResponse response = new StakeholderKpisResponse();
		response.setTotal(total);
		response.setPromoters(promoters);
		response.setNeutrals(neutrals);
		response.setCritics(critics);
		return response;
	}

	@Override
	public StakeholderGroupDetailResponse getGroupDetail(Long groupId, UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		StakeholderGroup group = groupRepository.findByIdAndCompanyId(groupId, companyId)
			.orElseThrow(() -> new RuntimeException("Stakeholder group not found: " + groupId));
		
		List<StakeholderPerson> persons = personRepository.findByGroupId(groupId);
		int participantCount = persons.size();

		Optional<Department> mappedDepartment = resolveDepartmentFromGroupName(group.getName());
		if (mappedDepartment.isPresent()) {
			participantCount = getSubmittedInstancesForDepartment(companyId, mappedDepartment.get(), null, null).size();
		}
		
		// Berechne aktuelle Readiness
		double readinessRaw = mappedDepartment.isPresent()
			? calculateDepartmentReadiness(companyId, mappedDepartment.get(), null, null)
			: calculateGroupReadiness(group, persons, companyId);
		
		// Berechne Promoter/Neutral/Critics
		String category = readinessCalculationService.calculatePromoterNeutralCritic(readinessRaw);
		int promoters = 0;
		int neutrals = 0;
		int critics = 0;
		
		if ("promoter".equals(category)) {
			promoters = participantCount;
		} else if ("neutral".equals(category)) {
			neutrals = participantCount;
		} else {
			critics = participantCount;
		}
		
		// Berechne Trend
		double previousReadinessRaw = mappedDepartment.isPresent()
			? calculateDepartmentReadiness(companyId, mappedDepartment.get(), null, LocalDateTime.now().minusDays(30))
			: calculateGroupReadiness30DaysAgo(group, persons, companyId);
		int trend = readinessCalculationService.calculateTrend(readinessRaw, previousReadinessRaw);
		
		// Berechne Status
		String status = readinessCalculationService.calculateStatus(readinessRaw);
		
		// Berechne Readiness-Historie (letzte 30 Tage)
		List<com.changeready.dto.stakeholder.ReadinessHistoryPointResponse> history = mappedDepartment.isPresent()
			? calculateDepartmentReadinessHistory(companyId, mappedDepartment.get())
			: calculateReadinessHistory(group, persons, companyId);
		
		StakeholderGroupDetailResponse response = new StakeholderGroupDetailResponse();
		response.setId(group.getId());
		response.setName(group.getName());
		response.setIcon(group.getIcon());
		response.setImpact(group.getImpact().getDisplayName());
		response.setDescription(group.getDescription());
		response.setParticipantCount(participantCount);
		response.setReadiness(roundPercent0(readinessRaw));
		response.setTrend(trend);
		response.setPromoters(promoters);
		response.setNeutrals(neutrals);
		response.setCritics(critics);
		response.setStatus(status);
		response.setHistory(history);
		
		return response;
	}

	/**
	 * Berechnet Readiness-Historie für eine Gruppe (letzte 30 Tage)
	 */
	private List<com.changeready.dto.stakeholder.ReadinessHistoryPointResponse> calculateReadinessHistory(
		StakeholderGroup group,
		List<StakeholderPerson> persons,
		Long companyId
	) {
		if (persons.isEmpty()) {
			return new ArrayList<>();
		}
		
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		
		// Sammle alle SUBMITTED Survey-Instanzen der Personen dieser Gruppe
		List<SurveyInstance> allInstances = new ArrayList<>();
		
		for (StakeholderPerson person : persons) {
			if (person.getEmail() != null && !person.getEmail().isEmpty()) {
				userRepository.findByEmail(person.getEmail())
					.ifPresent(user -> {
						List<SurveyInstance> userInstances = surveyInstanceRepository
							.findByUserIdAndCompanyId(user.getId(), companyId)
							.stream()
							.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
							.filter(instance -> instance.getSubmittedAt() != null)
							.filter(instance -> instance.getSubmittedAt().isAfter(thirtyDaysAgo))
							.collect(Collectors.toList());
						
						allInstances.addAll(userInstances);
					});
			}
		}
		
		if (allInstances.isEmpty()) {
			return new ArrayList<>();
		}
		
		// Gruppiere nach Datum
		Map<LocalDate, List<SurveyInstance>> instancesByDate = allInstances.stream()
			.collect(Collectors.groupingBy(instance -> instance.getSubmittedAt().toLocalDate()));
		
		// Erstelle Historie-Punkte
		List<com.changeready.dto.stakeholder.ReadinessHistoryPointResponse> history = new ArrayList<>();
		
		for (Map.Entry<LocalDate, List<SurveyInstance>> entry : instancesByDate.entrySet()) {
			LocalDate date = entry.getKey();
			List<SurveyInstance> instances = entry.getValue();
			
			// Sammle alle Antworten dieser Instanzen
			List<SurveyAnswer> allAnswers = new ArrayList<>();
			for (SurveyInstance instance : instances) {
				List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
				allAnswers.addAll(answers);
			}
			
			if (!allAnswers.isEmpty()) {
				double readiness = readinessCalculationService.calculateReadiness(allAnswers);
				
				com.changeready.dto.stakeholder.ReadinessHistoryPointResponse point =
					new com.changeready.dto.stakeholder.ReadinessHistoryPointResponse();
				point.setDate(date);
				point.setReadiness(readiness);
				history.add(point);
			}
		}
		
		// Sortiere nach Datum
		history.sort(Comparator.comparing(com.changeready.dto.stakeholder.ReadinessHistoryPointResponse::getDate));
		
		return history;
	}

	private Optional<Department> resolveDepartmentFromGroupName(String groupName) {
		if (groupName == null || groupName.isBlank()) {
			return Optional.empty();
		}
		String key = normalizeKey(groupName);
		return switch (key) {
			case "einkauf" -> Optional.of(Department.EINKAUF);
			case "vertrieb" -> Optional.of(Department.VERTRIEB);
			case "lagerlogistik", "lagerundlogistik", "lagerlogistikund" -> Optional.of(Department.LAGER_LOGISTIK);
			case "it" -> Optional.of(Department.IT);
			case "geschaeftsfuehrung", "geschaftsfuhrung", "geschaeftsfuehrungund" -> Optional.of(Department.GESCHAEFTSFUEHRUNG);
			default -> Optional.empty();
		};
	}

	private String normalizeKey(String input) {
		String s = input.trim().toLowerCase();
		s = s.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
		s = s.replace("&", "und");
		s = s.replaceAll("[^a-z0-9]+", "");
		return s;
	}

	private List<SurveyInstance> getSubmittedInstancesForDepartment(Long companyId, Department department, LocalDateTime after, LocalDateTime before) {
		return surveyInstanceRepository.findByCompanyIdAndStatus(companyId, SurveyInstance.SurveyInstanceStatus.SUBMITTED)
			.stream()
			.filter(i -> i.getDepartment() == department)
			.filter(i -> i.getSubmittedAt() != null)
			.filter(i -> after == null || i.getSubmittedAt().isAfter(after))
			.filter(i -> before == null || i.getSubmittedAt().isBefore(before))
			.collect(Collectors.toList());
	}

	private double calculateDepartmentReadiness(Long companyId, Department department, LocalDateTime after, LocalDateTime before) {
		List<SurveyInstance> instances = getSubmittedInstancesForDepartment(companyId, department, after, before);
		if (instances.isEmpty()) {
			return 0.0;
		}
		List<SurveyAnswer> allAnswers = new ArrayList<>();
		for (SurveyInstance instance : instances) {
			allAnswers.addAll(surveyAnswerRepository.findByInstanceId(instance.getId()));
		}
		if (allAnswers.isEmpty()) {
			return 0.0;
		}
		return readinessCalculationService.calculateReadiness(allAnswers);
	}

	private List<com.changeready.dto.stakeholder.ReadinessHistoryPointResponse> calculateDepartmentReadinessHistory(Long companyId, Department department) {
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		List<SurveyInstance> instances = getSubmittedInstancesForDepartment(companyId, department, thirtyDaysAgo, null);
		if (instances.isEmpty()) {
			return new ArrayList<>();
		}
		Map<LocalDate, List<SurveyInstance>> instancesByDate = instances.stream()
			.collect(Collectors.groupingBy(i -> i.getSubmittedAt().toLocalDate()));

		List<com.changeready.dto.stakeholder.ReadinessHistoryPointResponse> history = new ArrayList<>();
		for (Map.Entry<LocalDate, List<SurveyInstance>> entry : instancesByDate.entrySet()) {
			List<SurveyAnswer> answers = new ArrayList<>();
			for (SurveyInstance instance : entry.getValue()) {
				answers.addAll(surveyAnswerRepository.findByInstanceId(instance.getId()));
			}
			if (!answers.isEmpty()) {
				double readinessRaw = readinessCalculationService.calculateReadiness(answers);
				com.changeready.dto.stakeholder.ReadinessHistoryPointResponse point =
					new com.changeready.dto.stakeholder.ReadinessHistoryPointResponse();
				point.setDate(entry.getKey());
				point.setReadiness(roundPercent0(readinessRaw));
				history.add(point);
			}
		}
		history.sort(Comparator.comparing(com.changeready.dto.stakeholder.ReadinessHistoryPointResponse::getDate));
		return history;
	}

	private double roundPercent0(double value) {
		return (double) Math.round(value);
	}

	@Override
	public List<StakeholderPersonResponse> getGroupPersons(Long groupId, UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();
		
		// Prüfe ob Gruppe zur Company gehört
		groupRepository.findByIdAndCompanyId(groupId, companyId)
			.orElseThrow(() -> new RuntimeException("Stakeholder group not found: " + groupId));
		
		List<StakeholderPerson> persons = personRepository.findByGroupId(groupId);
		
		return persons.stream()
			.map(person -> {
				StakeholderPersonResponse response = toPersonResponse(person);
				
				// Berechne category (promoter/neutral/critic) basierend auf Readiness dieser Person
				if (person.getEmail() != null && !person.getEmail().isEmpty()) {
					double personReadiness = calculatePersonReadiness(person, companyId);
					if (personReadiness > 0) {
						String category = readinessCalculationService.calculatePromoterNeutralCritic(personReadiness);
						response.setCategory(category);
					} else {
						response.setCategory("neutral"); // Default wenn keine Daten vorhanden
					}
				} else {
					response.setCategory("neutral"); // Default wenn keine E-Mail vorhanden
				}
				
				return response;
			})
			.collect(Collectors.toList());
	}

	/**
	 * Berechnet Readiness für eine einzelne Stakeholder-Person
	 */
	private double calculatePersonReadiness(StakeholderPerson person, Long companyId) {
		if (person.getEmail() == null || person.getEmail().isEmpty()) {
			return 0.0;
		}
		
		return userRepository.findByEmail(person.getEmail())
			.map(user -> {
				List<SurveyInstance> userInstances = surveyInstanceRepository
					.findByUserIdAndCompanyId(user.getId(), companyId)
					.stream()
					.filter(instance -> instance.getStatus() == SurveyInstance.SurveyInstanceStatus.SUBMITTED)
					.collect(Collectors.toList());
				
				if (userInstances.isEmpty()) {
					return 0.0;
				}
				
				List<SurveyAnswer> allAnswers = new ArrayList<>();
				for (SurveyInstance instance : userInstances) {
					List<SurveyAnswer> answers = surveyAnswerRepository.findByInstanceId(instance.getId());
					allAnswers.addAll(answers);
				}
				
				if (allAnswers.isEmpty()) {
					return 0.0;
				}
				
				return readinessCalculationService.calculateReadiness(allAnswers);
			})
			.orElse(0.0);
	}

	@Override
	@Transactional
	public StakeholderGroupResponse createGroup(StakeholderGroupCreateRequest request, UserPrincipal userPrincipal) {
		// Company laden
		Company company = companyRepository.findById(userPrincipal.getCompanyId())
			.orElseThrow(() -> new RuntimeException("Company not found: " + userPrincipal.getCompanyId()));

		// Neue Gruppe erstellen
		StakeholderGroup group = new StakeholderGroup();
		group.setName(request.getName());
		String icon = request.getIcon();
		if (icon == null || icon.isBlank()) {
			icon = "people";
		}
		group.setIcon(icon);
		group.setImpact(request.getImpact());
		group.setDescription(request.getDescription());
		group.setCompany(company);

		group = groupRepository.save(group);

		return toGroupResponse(group);
	}

	@Override
	@Transactional
	public StakeholderGroupResponse updateGroup(Long groupId, StakeholderGroupUpdateRequest request, UserPrincipal userPrincipal) {
		// Gruppe laden mit Company-Isolation
		StakeholderGroup group = groupRepository.findByIdAndCompanyId(groupId, userPrincipal.getCompanyId())
			.orElseThrow(() -> new RuntimeException("Stakeholder group not found: " + groupId));

		// Felder aktualisieren (nur wenn gesetzt)
		if (request.getName() != null) {
			group.setName(request.getName());
		}
		if (request.getIcon() != null) {
			group.setIcon(request.getIcon());
		}
		if (request.getImpact() != null) {
			group.setImpact(request.getImpact());
		}
		if (request.getDescription() != null) {
			group.setDescription(request.getDescription());
		}

		group = groupRepository.save(group);

		return toGroupResponse(group);
	}

	@Override
	@Transactional
	public StakeholderPersonResponse addPerson(Long groupId, StakeholderPersonCreateRequest request, UserPrincipal userPrincipal) {
		// Gruppe laden mit Company-Isolation
		StakeholderGroup group = groupRepository.findByIdAndCompanyId(groupId, userPrincipal.getCompanyId())
			.orElseThrow(() -> new RuntimeException("Stakeholder group not found: " + groupId));

		// Neue Person erstellen
		StakeholderPerson person = new StakeholderPerson();
		person.setGroup(group);
		person.setName(request.getName());
		person.setRole(request.getRole());
		person.setEmail(request.getEmail());

		person = personRepository.save(person);

		return toPersonResponse(person);
	}

	@Override
	@Transactional
	public void deleteGroup(Long groupId, UserPrincipal userPrincipal) {
		Long companyId = userPrincipal.getCompanyId();

		StakeholderGroup group = groupRepository.findByIdAndCompanyId(groupId, companyId)
			.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Stakeholder group not found: " + groupId));

		// Erst Personen löschen (FK), dann Gruppe
		personRepository.deleteByGroupId(groupId);
		groupRepository.delete(group);
	}

	// Helper-Methoden für Mapping

	private StakeholderGroupResponse toGroupResponse(StakeholderGroup group) {
		StakeholderGroupResponse response = new StakeholderGroupResponse();
		response.setId(group.getId());
		response.setName(group.getName());
		response.setIcon(group.getIcon());
		response.setImpact(group.getImpact().getDisplayName());
		// TODO: Readiness, trend, promoters, neutrals, critics, status werden später berechnet
		response.setParticipantCount(0);
		response.setReadiness(0.0);
		response.setTrend(0);
		response.setPromoters(0);
		response.setNeutrals(0);
		response.setCritics(0);
		response.setStatus("ready");
		return response;
	}

	private StakeholderPersonResponse toPersonResponse(StakeholderPerson person) {
		StakeholderPersonResponse response = new StakeholderPersonResponse();
		response.setId(person.getId());
		response.setName(person.getName());
		response.setRole(person.getRole());
		// TODO: category (promoter/neutral/critic) wird später aus Readiness berechnet
		response.setCategory("neutral");
		return response;
	}
}

