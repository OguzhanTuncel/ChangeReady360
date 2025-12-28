package com.changeready.dto.stakeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderKpisResponse {

	private Integer total;
	private Integer promoters;
	private Integer neutrals;
	private Integer critics;
}

