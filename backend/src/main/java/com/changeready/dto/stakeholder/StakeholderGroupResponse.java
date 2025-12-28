package com.changeready.dto.stakeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderGroupResponse {

	private Long id;
	private String name;
	private String icon; // Material icon name
	private Integer participantCount;
	private Double readiness; // 0-100%
	private Integer trend; // e.g., +5, -3, 0 (in Prozentpunkten)
	private Integer promoters;
	private Integer neutrals;
	private Integer critics;
	private String status; // "ready", "attention", "critical"
	private String impact; // "Niedrig", "Mittel", "Hoch", "Sehr hoch", "Strategisch"
}

