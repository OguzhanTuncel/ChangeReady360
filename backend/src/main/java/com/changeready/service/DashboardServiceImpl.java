package com.changeready.service;

import com.changeready.dto.dashboard.DashboardKpisResponse;
import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DashboardServiceImpl implements DashboardService {

	@Override
	public DashboardKpisResponse getKpis(UserPrincipal userPrincipal) {
		// TODO: Implementiere echte Datenaggregation aus Survey/Stakeholder Entities
		// Aktuell: Platzhalter-Daten zurückgeben
		DashboardKpisResponse response = new DashboardKpisResponse();
		response.setTotalSurveys(0);
		response.setCompletedSurveys(0);
		response.setOpenSurveys(0);
		response.setTotalStakeholders(0);
		response.setPromoters(0);
		response.setNeutrals(0);
		response.setCritics(0);
		response.setOverallReadiness(0.0);
		response.setActiveMeasures(0);
		return response;
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

