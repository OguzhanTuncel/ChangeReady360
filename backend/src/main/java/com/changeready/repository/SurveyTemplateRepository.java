package com.changeready.repository;

import com.changeready.entity.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, Long> {

	/**
	 * Findet alle aktiven Templates
	 * @param active true für aktive Templates
	 * @return Liste von aktiven Templates
	 */
	List<SurveyTemplate> findByActive(Boolean active);

	/**
	 * Findet alle Templates einer Company
	 * @param companyId Company-ID
	 * @return Liste von Templates der Company
	 */
	List<SurveyTemplate> findByCompanyId(Long companyId);

	/**
	 * Findet alle aktiven Templates einer Company
	 * @param companyId Company-ID
	 * @param active true für aktive Templates
	 * @return Liste von aktiven Templates der Company
	 */
	List<SurveyTemplate> findByCompanyIdAndActive(Long companyId, Boolean active);
}

