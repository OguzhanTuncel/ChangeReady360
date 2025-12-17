package com.changeready.controller;

import com.changeready.dto.invite.InviteRequest;
import com.changeready.dto.invite.InviteResponse;
import com.changeready.dto.invite.PasswordSetupRequest;
import com.changeready.service.InviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invites")
public class InviteController {

	private final InviteService inviteService;

	public InviteController(InviteService inviteService) {
		this.inviteService = inviteService;
	}

	/**
	 * SYSTEM_ADMIN: Erstellt eine Einladung für einen COMPANY_ADMIN
	 * Erstellt die Company automatisch falls sie noch nicht existiert
	 */
	@PostMapping("/company-admin")
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public ResponseEntity<InviteResponse> createCompanyAdminInvite(
		@Valid @RequestBody InviteRequest request
	) {
		InviteResponse response = inviteService.createCompanyAdminInvite(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * COMPANY_ADMIN: Erstellt eine Einladung für einen COMPANY_USER
	 * Nutzer wird automatisch der Company des eingeloggten COMPANY_ADMIN zugeordnet
	 */
	@PostMapping("/company-user")
	@PreAuthorize("hasRole('COMPANY_ADMIN')")
	public ResponseEntity<InviteResponse> createCompanyUserInvite(
		@Valid @RequestBody InviteRequest request
	) {
		InviteResponse response = inviteService.createCompanyUserInvite(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * SYSTEM_ADMIN: Holt alle Invites
	 */
	@GetMapping
	@PreAuthorize("hasRole('SYSTEM_ADMIN')")
	public ResponseEntity<List<InviteResponse>> getAllInvites() {
		List<InviteResponse> invites = inviteService.findAll();
		return ResponseEntity.ok(invites);
	}

	/**
	 * COMPANY_ADMIN: Holt alle Invites seiner Company
	 */
	@GetMapping("/my-company")
	@PreAuthorize("hasRole('COMPANY_ADMIN')")
	public ResponseEntity<List<InviteResponse>> getMyCompanyInvites() {
		List<InviteResponse> invites = inviteService.findAllByCompany();
		return ResponseEntity.ok(invites);
	}

	/**
	 * Öffentlicher Endpoint: Validiert einen Invite-Token
	 * Wird verwendet um zu prüfen ob ein Token gültig ist bevor das Passwort gesetzt wird
	 */
	@GetMapping("/validate/{token}")
	public ResponseEntity<InviteResponse> validateToken(@PathVariable String token) {
		InviteResponse response = inviteService.validateToken(token);
		return ResponseEntity.ok(response);
	}

	/**
	 * Öffentlicher Endpoint: Akzeptiert eine Einladung und erstellt den User mit Passwort
	 */
	@PostMapping("/accept")
	public ResponseEntity<Void> acceptInvite(@Valid @RequestBody PasswordSetupRequest request) {
		inviteService.acceptInvite(request);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}

