package com.changeready.dto.stakeholder;

import com.changeready.entity.StakeholderGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderGroupUpdateRequest {
	/**
	 * Alle Felder sind optional für Update
	 */
	private String name;
	private String icon;
	private StakeholderGroup.Impact impact;
	private String description;
}

