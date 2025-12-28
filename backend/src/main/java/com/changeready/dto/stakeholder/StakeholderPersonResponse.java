package com.changeready.dto.stakeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderPersonResponse {

	private Long id;
	private String name;
	private String role; // Optional
	private String category; // "promoter", "neutral", "critic"
}

