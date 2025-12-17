package com.changeready.dto.company;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

	private Long id;
	private String name;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

