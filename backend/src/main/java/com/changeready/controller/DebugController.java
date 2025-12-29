package com.changeready.controller;

import com.changeready.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/debug")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'COMPANY_ADMIN')")
public class DebugController {

	@GetMapping("/auth-info")
	public ResponseEntity<Map<String, Object>> getAuthInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		Map<String, Object> info = new HashMap<>();
		
		if (authentication == null) {
			info.put("error", "No authentication found");
			return ResponseEntity.ok(info);
		}
		
		info.put("authenticated", authentication.isAuthenticated());
		info.put("principalType", authentication.getPrincipal().getClass().getName());
		
		if (authentication.getPrincipal() instanceof UserPrincipal) {
			UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
			info.put("userId", userPrincipal.getId());
			info.put("email", userPrincipal.getEmail());
			info.put("role", userPrincipal.getRole().name());
			info.put("companyId", userPrincipal.getCompanyId());
			info.put("active", userPrincipal.getActive());
		}
		
		info.put("authorities", authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toList()));
		
		info.put("authorityCount", authentication.getAuthorities().size());
		
		return ResponseEntity.ok(info);
	}
}

