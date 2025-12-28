package com.changeready.service;

import com.changeready.entity.SurveyAnswer;

import java.util.List;

public interface ReadinessCalculationService {

	/**
	 * Berechnet Readiness aus Survey-Antworten
	 * Formel: ((Durchschnitt - 1) / 4) * 100
	 * @param answers Liste von Survey-Antworten (Werte 1-5)
	 * @return Readiness-Wert (0-100%)
	 */
	double calculateReadiness(List<SurveyAnswer> answers);

	/**
	 * Kategorisiert Readiness-Wert in Promoter/Neutral/Kritiker
	 * @param readiness Readiness-Wert (0-100%)
	 * @return Kategorie: "promoter" (>=75%), "neutral" (50-75%), "critic" (<50%)
	 */
	String calculatePromoterNeutralCritic(double readiness);

	/**
	 * Berechnet Trend zwischen zwei Readiness-Werten
	 * @param currentReadiness Aktueller Readiness-Wert
	 * @param previousReadiness Vorheriger Readiness-Wert
	 * @return Trend als Integer: positiv = besser, negativ = schlechter, 0 = gleich
	 */
	int calculateTrend(double currentReadiness, double previousReadiness);

	/**
	 * Berechnet Status basierend auf Readiness-Wert
	 * @param readiness Readiness-Wert (0-100%)
	 * @return Status: "ready" (>=75%), "attention" (50-75%), "critical" (<50%)
	 */
	String calculateStatus(double readiness);
}

