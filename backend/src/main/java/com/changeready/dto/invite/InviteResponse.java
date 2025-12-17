package com.changeready.dto.invite;

import com.changeready.entity.Invite;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponse {

	private Long id;
	private String email;
	private String role;
	private Long companyId;
	private String companyName;
	private String token;
	private Invite.InviteStatus status;
	private LocalDateTime expiresAt;
	private Long createdBy;
	private LocalDateTime acceptedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

