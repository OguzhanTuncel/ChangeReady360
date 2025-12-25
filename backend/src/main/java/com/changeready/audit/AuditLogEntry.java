package com.changeready.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SEC-012: Audit Log Entry
 * Records critical actions in the system for security and compliance
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

	private LocalDateTime timestamp;
	private String action;
	private Long userId;
	private String userRole;
	private Long targetEntityId;
	private String targetEntityType;
	private String ipAddress;
	private boolean success;
	private String failureReason;

	public static AuditLogEntry success(String action, Long userId, String userRole, Long targetEntityId, String targetEntityType, String ipAddress) {
		AuditLogEntry entry = new AuditLogEntry();
		entry.setTimestamp(LocalDateTime.now());
		entry.setAction(action);
		entry.setUserId(userId);
		entry.setUserRole(userRole);
		entry.setTargetEntityId(targetEntityId);
		entry.setTargetEntityType(targetEntityType);
		entry.setIpAddress(ipAddress);
		entry.setSuccess(true);
		return entry;
	}

	public static AuditLogEntry failure(String action, Long userId, String userRole, String ipAddress, String reason) {
		AuditLogEntry entry = new AuditLogEntry();
		entry.setTimestamp(LocalDateTime.now());
		entry.setAction(action);
		entry.setUserId(userId);
		entry.setUserRole(userRole);
		entry.setIpAddress(ipAddress);
		entry.setSuccess(false);
		entry.setFailureReason(reason);
		return entry;
	}
}

