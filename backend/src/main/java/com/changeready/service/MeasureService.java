package com.changeready.service;

import com.changeready.dto.measure.MeasureResponse;
import com.changeready.security.UserPrincipal;

import java.util.List;

public interface MeasureService {

	/**
	 * Lädt alle aktiven Maßnahmen für die Company des Benutzers
	 * Aktive Maßnahmen sind OPEN oder IN_PROGRESS
	 */
	List<MeasureResponse> getActiveMeasures(UserPrincipal userPrincipal);
}

