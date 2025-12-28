package com.changeready.service;

import com.changeready.entity.SurveyAnswer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadinessCalculationServiceImpl implements ReadinessCalculationService {

	@Override
	public double calculateReadiness(List<SurveyAnswer> answers) {
		if (answers == null || answers.isEmpty()) {
			return 0.0;
		}

		// Durchschnitt aller Antwort-Werte berechnen
		double sum = 0.0;
		for (SurveyAnswer answer : answers) {
			sum += answer.getValue();
		}
		double average = sum / answers.size();

		// Formel: ((Durchschnitt - 1) / 4) * 100
		// Beispiel: Durchschnitt 3.5 → ((3.5 - 1) / 4) * 100 = (2.5 / 4) * 100 = 62.5%
		double readiness = ((average - 1.0) / 4.0) * 100.0;

		// Sicherstellen, dass Wert zwischen 0 und 100 liegt
		return Math.max(0.0, Math.min(100.0, readiness));
	}

	@Override
	public String calculatePromoterNeutralCritic(double readiness) {
		if (readiness >= 75.0) {
			return "promoter";
		} else if (readiness >= 50.0) {
			return "neutral";
		} else {
			return "critic";
		}
	}

	@Override
	public int calculateTrend(double currentReadiness, double previousReadiness) {
		// Trend = aktueller Wert - vorheriger Wert
		// Positiv = besser, negativ = schlechter, 0 = gleich
		double difference = currentReadiness - previousReadiness;
		
		// Runden auf ganze Zahl (Prozentpunkte)
		return (int) Math.round(difference);
	}

	@Override
	public String calculateStatus(double readiness) {
		if (readiness >= 75.0) {
			return "ready";
		} else if (readiness >= 50.0) {
			return "attention";
		} else {
			return "critical";
		}
	}
}

