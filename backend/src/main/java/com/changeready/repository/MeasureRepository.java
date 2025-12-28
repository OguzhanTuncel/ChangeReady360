package com.changeready.repository;

import com.changeready.entity.Measure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeasureRepository extends JpaRepository<Measure, Long> {

	/**
	 * Findet alle aktiven Maßnahmen einer Company
	 * Aktive Maßnahmen sind OPEN oder IN_PROGRESS
	 */
	List<Measure> findByCompanyIdAndStatusIn(Long companyId, List<Measure.MeasureStatus> statuses);

	/**
	 * Findet alle Maßnahmen einer Company
	 */
	List<Measure> findByCompanyId(Long companyId);
}

