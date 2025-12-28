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
		// TODO: Implementiere echte Daten aus Stakeholder Entities mit Readiness-Berechnung
		// Aktuell: Leere Liste zurückgeben
		return new ArrayList<>();
	}

	@Override
	public StakeholderKpisResponse getKpis(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte KPI-Berechnung aus Stakeholder Daten
		// Aktuell: Platzhalter-Daten zurückgeben
		StakeholderKpisResponse response = new StakeholderKpisResponse();
		response.setTotal(0);
		response.setPromoters(0);
		response.setNeutrals(0);
		response.setCritics(0);
		return response;
	}

	@Override
	public StakeholderGroupDetailResponse getGroupDetail(Long groupId, UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Daten aus Stakeholder Entities
		// Aktuell: Exception werfen, da keine Daten vorhanden
		throw new RuntimeException("Stakeholder group not found: " + groupId);
	}

	@Override
	public List<StakeholderPersonResponse> getGroupPersons(Long groupId, UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Daten aus Stakeholder Entities
		// Aktuell: Leere Liste zurückgeben
		return new ArrayList<>();
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

