package com.changeready.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpisResponse {

	private Integer totalSurveys;
	private Integer completedSurveys;
	private Integer openSurveys;
	private Integer totalStakeholders;
	private Integer promoters;
	private Integer neutrals;
	private Integer critics;
	private Double overallReadiness;
	private Integer activeMeasures;
}


