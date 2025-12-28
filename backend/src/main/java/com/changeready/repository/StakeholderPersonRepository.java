package com.changeready.repository;

import com.changeready.entity.StakeholderPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StakeholderPersonRepository extends JpaRepository<StakeholderPerson, Long> {

	/**
	 * Findet alle Personen einer Stakeholder-Gruppe
	 * @param groupId Gruppen-ID
	 * @return Liste von StakeholderPersons der Gruppe
	 */
	List<StakeholderPerson> findByGroupId(Long groupId);
}

