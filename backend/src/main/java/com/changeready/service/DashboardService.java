package com.changeready.service;

import com.changeready.dto.dashboard.DashboardKpisResponse;
import com.changeready.dto.dashboard.TrendDataResponse;
import com.changeready.security.UserPrincipal;

public interface DashboardService {

	/**
	 * Lädt Dashboard-KPIs für den aktuellen Benutzer
	 * Berücksichtigt die Company des Benutzers und dessen Rolle
	 */
	DashboardKpisResponse getKpis(UserPrincipal userPrincipal);

	/**
	 * Lädt Trend-Daten für den Readiness-Verlauf
	 * Gibt historische Datenpunkte zurück (letzte 30 Tage)
	 */
	TrendDataResponse getTrendData(UserPrincipal userPrincipal);
}

