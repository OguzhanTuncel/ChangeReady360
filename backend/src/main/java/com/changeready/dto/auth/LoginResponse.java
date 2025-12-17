package com.changeready.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

	private String token;
	private String tokenType = "Bearer";
	private UserInfo userInfo;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo {
		private Long id;
		private String email;
		private String role;
		private Long companyId;
	}
}

