package com.changeready.service;

import com.changeready.dto.stakeholder.StakeholderGroupDetailResponse;
import com.changeready.dto.stakeholder.StakeholderGroupResponse;
import com.changeready.dto.stakeholder.StakeholderKpisResponse;
import com.changeready.dto.stakeholder.StakeholderPersonResponse;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StakeholderServiceImpl implements StakeholderService {

	@Override
	public List<StakeholderGroupResponse> getGroups(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Daten aus Stakeholder Entities
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
}

