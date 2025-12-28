package com.changeready.repository;

import com.changeready.entity.SurveyInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyInstanceRepository extends JpaRepository<SurveyInstance, Long> {

	/**
	 * Findet alle Instanzen eines Users innerhalb seiner Company
	 * @param userId User-ID
	 * @param companyId Company-ID
	 * @return Liste von SurveyInstances des Users
	 */
	List<SurveyInstance> findByUserIdAndCompanyId(Long userId, Long companyId);

	/**
	 * Findet alle Instanzen einer Company mit bestimmten Status
	 * @param companyId Company-ID
	 * @param status Status (DRAFT oder SUBMITTED)
	 * @return Liste von SurveyInstances mit dem Status
	 */
	List<SurveyInstance> findByCompanyIdAndStatus(Long companyId, SurveyInstance.SurveyInstanceStatus status);

	/**
	 * Findet alle Instanzen eines Templates innerhalb einer Company
	 * @param templateId Template-ID
	 * @param companyId Company-ID
	 * @return Liste von SurveyInstances des Templates
	 */
	List<SurveyInstance> findByTemplateIdAndCompanyId(Long templateId, Long companyId);

	/**
	 * Findet alle Instanzen einer Company
	 * @param companyId Company-ID
	 * @return Liste von SurveyInstances der Company
	 */
	List<SurveyInstance> findByCompanyId(Long companyId);
}

