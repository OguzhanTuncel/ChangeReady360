package com.changeready.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SEC-012: Audit Logger
 * Logs critical security events for compliance and forensics
 * 
 * Critical actions logged:
 * - User creation (SYSTEM_ADMIN creates COMPANY_ADMIN, COMPANY_ADMIN creates COMPANY_USER)
 * - Company creation
 * - User deactivation
 * - Role changes (if implemented)
 * - Login failures (if rate limiting is implemented)
 */
@Component
public class AuditLogger {

	private static final Logger logger = LoggerFactory.getLogger("AUDIT");

	public void log(AuditLogEntry entry) {
		if (entry.isSuccess()) {
			logger.info("AUDIT | Action: {} | User: {} (Role: {}) | Target: {} (ID: {}) | IP: {} | Status: SUCCESS",
				entry.getAction(),
				entry.getUserId(),
				entry.getUserRole(),
				entry.getTargetEntityType(),
				entry.getTargetEntityId(),
				entry.getIpAddress());
		} else {
			logger.warn("AUDIT | Action: {} | User: {} (Role: {}) | IP: {} | Status: FAILURE | Reason: {}",
				entry.getAction(),
				entry.getUserId(),
				entry.getUserRole(),
				entry.getIpAddress(),
				entry.getFailureReason());
		}
	}

	// Convenience methods for common audit events

	public void logCompanyCreated(Long adminId, String adminRole, Long companyId, String ipAddress) {
		log(AuditLogEntry.success("COMPANY_CREATED", adminId, adminRole, companyId, "Company", ipAddress));
	}

	public void logUserCreated(Long creatorId, String creatorRole, Long userId, String userRole, String ipAddress) {
		log(AuditLogEntry.success("USER_CREATED", creatorId, creatorRole, userId, "User:" + userRole, ipAddress));
	}

	public void logUserDeactivated(Long adminId, String adminRole, Long userId, String ipAddress) {
		log(AuditLogEntry.success("USER_DEACTIVATED", adminId, adminRole, userId, "User", ipAddress));
	}

	public void logLoginSuccess(Long userId, String userRole, String ipAddress) {
		log(AuditLogEntry.success("LOGIN_SUCCESS", userId, userRole, null, null, ipAddress));
	}

	public void logLoginFailure(String email, String ipAddress, String reason) {
		log(AuditLogEntry.failure("LOGIN_FAILED", null, null, ipAddress, reason + " (Email: " + email + ")"));
	}

	public void logUnauthorizedAccess(Long userId, String userRole, String action, String ipAddress) {
		log(AuditLogEntry.failure("UNAUTHORIZED_ACCESS", userId, userRole, ipAddress, action));
	}
}

