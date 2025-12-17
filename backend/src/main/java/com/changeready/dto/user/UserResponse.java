package com.changeready.dto.user;

import com.changeready.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private Long id;
	private String email;
	private Role role;
	private Long companyId;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

