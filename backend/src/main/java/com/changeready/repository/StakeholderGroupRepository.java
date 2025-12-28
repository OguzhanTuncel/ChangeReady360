package com.changeready.repository;

import com.changeready.entity.StakeholderGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StakeholderGroupRepository extends JpaRepository<StakeholderGroup, Long> {

	/**
	 * Findet alle Stakeholder-Gruppen einer Company
	 * @param companyId Company-ID
	 * @return Liste von StakeholderGroups der Company
	 */
	List<StakeholderGroup> findByCompanyId(Long companyId);

	/**
	 * Findet eine spezifische Stakeholder-Gruppe einer Company
	 * @param id Gruppen-ID
	 * @param companyId Company-ID
	 * @return Optional StakeholderGroup
	 */
	Optional<StakeholderGroup> findByIdAndCompanyId(Long id, Long companyId);
}

