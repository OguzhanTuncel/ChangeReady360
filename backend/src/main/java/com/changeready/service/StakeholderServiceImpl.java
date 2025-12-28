package com.changeready.service;

import com.changeready.dto.stakeholder.StakeholderGroupCreateRequest;
import com.changeready.dto.stakeholder.StakeholderGroupDetailResponse;
import com.changeready.dto.stakeholder.StakeholderGroupResponse;
import com.changeready.dto.stakeholder.StakeholderGroupUpdateRequest;
import com.changeready.dto.stakeholder.StakeholderKpisResponse;
import com.changeready.dto.stakeholder.StakeholderPersonCreateRequest;
import com.changeready.dto.stakeholder.StakeholderPersonResponse;
import com.changeready.entity.Company;
import com.changeready.entity.StakeholderGroup;
import com.changeready.entity.StakeholderPerson;
import com.changeready.repository.CompanyRepository;
import com.changeready.repository.StakeholderGroupRepository;
import com.changeready.repository.StakeholderPersonRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StakeholderServiceImpl implements StakeholderService {

	private final StakeholderGroupRepository groupRepository;
	private final StakeholderPersonRepository personRepository;
	private final CompanyRepository companyRepository;

	public StakeholderServiceImpl(
		StakeholderGroupRepository groupRepository,
		StakeholderPersonRepository personRepository,
		CompanyRepository companyRepository
	) {
		this.groupRepository = groupRepository;
		this.personRepository = personRepository;
		this.companyRepository = companyRepository;
	}

	@Override
	public List<StakeholderGroupResponse> getGroups(UserPrincipal userPrincipal) {
		List<StakeholderGroup> groups = groupRepository.findByCompanyId(userPrincipal.getCompanyId());
		
		return groups.stream()
			.map(group -> {
				StakeholderGroupResponse response = toGroupResponse(group);
				// Participant count aus Personen berechnen
				int participantCount = personRepository.findByGroupId(group.getId()).size();
				response.setParticipantCount(participantCount);
				// TODO: Readiness, trend, promoters, neutrals, critics, status werden in Task 5.0 berechnet
				return response;
			})
			.collect(java.util.stream.Collectors.toList());
	}

	@Override
	public StakeholderKpisResponse getKpis(UserPrincipal userPrincipal) {
		List<StakeholderGroup> groups = groupRepository.findByCompanyId(userPrincipal.getCompanyId());
		
		int total = 0;
		for (StakeholderGroup group : groups) {
			total += personRepository.findByGroupId(group.getId()).size();
		}
		
		StakeholderKpisResponse response = new StakeholderKpisResponse();
		response.setTotal(total);
		// TODO: promoters, neutrals, critics werden in Task 5.0 aus Readiness-Berechnung abgeleitet
		response.setPromoters(0);
		response.setNeutrals(0);
		response.setCritics(0);
		return response;
	}

	@Override
	public StakeholderGroupDetailResponse getGroupDetail(Long groupId, UserPrincipal userPrincipal) {
		StakeholderGroup group = groupRepository.findByIdAndCompanyId(groupId, userPrincipal.getCompanyId())
			.orElseThrow(() -> new RuntimeException("Stakeholder group not found: " + groupId));
		
		StakeholderGroupDetailResponse response = new StakeholderGroupDetailResponse();
		response.setId(group.getId());
		response.setName(group.getName());
		response.setIcon(group.getIcon());
		response.setImpact(group.getImpact().getDisplayName());
		response.setDescription(group.getDescription());
		
		// Participant count
		int participantCount = personRepository.findByGroupId(group.getId()).size();
		response.setParticipantCount(participantCount);
		
		// TODO: Readiness, trend, promoters, neutrals, critics, status werden in Task 5.0 berechnet
		response.setReadiness(0.0);
		response.setTrend(0);
		response.setPromoters(0);
		response.setNeutrals(0);
		response.setCritics(0);
		response.setStatus("ready");
		
		// TODO: Historical readiness wird in Task 5.0 berechnet
		response.setHistoricalReadiness(new ArrayList<>());
		response.setLastUpdated(LocalDateTime.now());
		
		return response;
	}

	@Override
	public List<StakeholderPersonResponse> getGroupPersons(Long groupId, UserPrincipal userPrincipal) {
		// Prüfe ob Gruppe zur Company gehört
		StakeholderGroup group = groupRepository.findByIdAndCompanyId(groupId, userPrincipal.getCompanyId())
			.orElseThrow(() -> new RuntimeException("Stakeholder group not found: " + groupId));
		
		List<StakeholderPerson> persons = personRepository.findByGroupId(groupId);
		
		return persons.stream()
			.map(person -> {
				StakeholderPersonResponse response = toPersonResponse(person);
				// TODO: category (promoter/neutral/critic) wird in Task 5.0 aus Readiness berechnet
				return response;
			})
			.collect(java.util.stream.Collectors.toList());
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
		group.setIcon(request.getIcon());
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

