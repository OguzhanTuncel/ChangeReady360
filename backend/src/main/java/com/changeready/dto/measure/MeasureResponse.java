package com.changeready.dto.measure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasureResponse {

	private Long id;
	private String title;
	private String description;
	private String status; // "OPEN", "IN_PROGRESS", "COMPLETED", "CANCELLED"
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}


