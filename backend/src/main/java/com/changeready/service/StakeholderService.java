package com.changeready.service;

import com.changeready.dto.stakeholder.StakeholderGroupCreateRequest;
import com.changeready.dto.stakeholder.StakeholderGroupDetailResponse;
import com.changeready.dto.stakeholder.StakeholderGroupResponse;
import com.changeready.dto.stakeholder.StakeholderGroupUpdateRequest;
import com.changeready.dto.stakeholder.StakeholderKpisResponse;
import com.changeready.dto.stakeholder.StakeholderPersonCreateRequest;
import com.changeready.dto.stakeholder.StakeholderPersonResponse;
import com.changeready.security.UserPrincipal;

import java.util.List;

public interface StakeholderService {

	/**
	 * Lädt alle Stakeholder-Gruppen für die Company des Benutzers
	 */
	List<StakeholderGroupResponse> getGroups(UserPrincipal userPrincipal);

	/**
	 * Lädt Stakeholder-KPIs für die Company des Benutzers
	 */
	StakeholderKpisResponse getKpis(UserPrincipal userPrincipal);

	/**
	 * Lädt Details einer Stakeholder-Gruppe
	 */
	StakeholderGroupDetailResponse getGroupDetail(Long groupId, UserPrincipal userPrincipal);

	/**
	 * Lädt Personen einer Stakeholder-Gruppe
	 */
	List<StakeholderPersonResponse> getGroupPersons(Long groupId, UserPrincipal userPrincipal);

	/**
	 * Erstellt eine neue Stakeholder-Gruppe
	 */
	StakeholderGroupResponse createGroup(StakeholderGroupCreateRequest request, UserPrincipal userPrincipal);

	/**
	 * Aktualisiert eine Stakeholder-Gruppe
	 */
	StakeholderGroupResponse updateGroup(Long groupId, StakeholderGroupUpdateRequest request, UserPrincipal userPrincipal);

	/**
	 * Fügt eine Person zu einer Stakeholder-Gruppe hinzu
	 */
	StakeholderPersonResponse addPerson(Long groupId, StakeholderPersonCreateRequest request, UserPrincipal userPrincipal);
}

