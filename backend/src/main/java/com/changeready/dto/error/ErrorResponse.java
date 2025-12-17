package com.changeready.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private String error;
	private String code;
	private Instant timestamp;

	public ErrorResponse(String error, String code) {
		this.error = error;
		this.code = code;
		this.timestamp = Instant.now();
	}
}

