package com.changeready.dto.stakeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeholderGroupDetailResponse extends StakeholderGroupResponse {

	private String description; // Optional
	private List<ReadinessHistoryPointResponse> history;
	private List<StakeholderPersonResponse> persons;
}

