package com.changeready.config;

import com.changeready.entity.SurveyTemplate;
import com.changeready.repository.SurveyTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Seeds at least one global SurveyTemplate so the frontend can load /api/v1/surveys/templates backend-driven.
 *
 * Safe behavior:
 * - Does nothing if there is already any active template.
 * - Can be disabled via app.seed.surveys.enabled=false (recommended for production once templates are managed explicitly).
 */
@Component
public class InitialSurveyTemplateSetup implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(InitialSurveyTemplateSetup.class);

	private final SurveyTemplateRepository surveyTemplateRepository;

	@Value("${app.seed.surveys.enabled:true}")
	private boolean enabled;

	public InitialSurveyTemplateSetup(SurveyTemplateRepository surveyTemplateRepository) {
		this.surveyTemplateRepository = surveyTemplateRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (!enabled) {
			logger.info("Survey template seeding disabled (app.seed.surveys.enabled=false).");
			return;
		}

		try {
			List<SurveyTemplate> activeTemplates = surveyTemplateRepository.findByActive(true);

			// If the full template already exists, do nothing.
			boolean fullTemplateExists = activeTemplates.stream().anyMatch(t ->
				"Change Readiness – Standard".equals(t.getName()) && "1.1".equals(t.getVersion())
			);
			if (fullTemplateExists) {
				logger.info("Full SurveyTemplate already exists (Change Readiness – Standard v1.1). Skipping template seeding.");
				return;
			}

			// If there's an older seed template that is incomplete, deactivate it to avoid duplicates in the UI.
			// This is data-only (no endpoint/DTO breaking) and existing instances can still load their template via instance detail.
			activeTemplates.stream()
				.filter(t -> "ChangeReady360 – Standard".equals(t.getName()) && "1.0".equals(t.getVersion()))
				.forEach(t -> {
					int approxQuestionCount = countApproxQuestions(t.getCategoriesJson());
					if (approxQuestionCount > 0 && approxQuestionCount < 20) {
						t.setActive(false);
						surveyTemplateRepository.save(t);
						logger.info("Deactivated incomplete seed template (id={}, name={}, version={}, approxQuestions={})",
							t.getId(), t.getName(), t.getVersion(), approxQuestionCount);
					}
				});

			String categoriesJson = new String(
				new ClassPathResource("seeds/changeready360-standard-v1_1.categories.json").getInputStream().readAllBytes(),
				StandardCharsets.UTF_8
			);

			SurveyTemplate template = new SurveyTemplate();
			template.setName("Change Readiness – Standard");
			template.setDescription("Standard ChangeReady360 Fragebogen (Likert 1–5) mit Template-Variablen {Artikel} und {Projekt}.");
			template.setVersion("1.1");
			template.setActive(true);
			template.setCategoriesJson(categoriesJson);
			template.setCompany(null); // global template for all companies

			SurveyTemplate saved = surveyTemplateRepository.save(template);
			logger.info("Seeded initial global SurveyTemplate (id={})", saved.getId());
		} catch (Exception e) {
			// Do not crash startup; just log and continue so system remains usable.
			logger.error("Failed to seed initial SurveyTemplate. Surveys page will be empty until templates are created. Reason: {}", e.getMessage(), e);
		}
	}

	private int countApproxQuestions(String categoriesJson) {
		if (categoriesJson == null || categoriesJson.isBlank()) {
			return 0;
		}
		// quick heuristic: count occurrences of \"id\" in the stored JSON
		int count = 0;
		int idx = 0;
		while ((idx = categoriesJson.indexOf("\"id\"", idx)) != -1) {
			count++;
			idx += 4;
		}
		return count;
	}
}
