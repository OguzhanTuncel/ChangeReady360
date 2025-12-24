package com.changeready.service;

import com.changeready.dto.invite.InviteRequest;
import com.changeready.dto.invite.InviteResponse;
import com.changeready.dto.invite.PasswordSetupRequest;
import com.changeready.entity.Company;
import com.changeready.entity.Invite;
import com.changeready.entity.Role;
import com.changeready.entity.User;
import com.changeready.exception.ResourceNotFoundException;
import com.changeready.exception.ValidationException;
import com.changeready.repository.CompanyRepository;
import com.changeready.repository.InviteRepository;
import com.changeready.repository.UserRepository;
import com.changeready.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InviteService {

	private static final int TOKEN_LENGTH = 64;
	private static final int INVITE_VALIDITY_DAYS = 7;

	private final InviteRepository inviteRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public InviteService(
		InviteRepository inviteRepository,
		CompanyRepository companyRepository,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder
	) {
		this.inviteRepository = inviteRepository;
		this.companyRepository = companyRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * @deprecated Invite-System wurde deaktiviert. 
	 * Verwenden Sie stattdessen UserService.createCompanyAdmin()
	 * 
	 * Diese Methode erstellt KEINE Company mehr automatisch.
	 * Companies müssen manuell über CompanyService erstellt werden.
	 */
	@Deprecated
	@Transactional
	public InviteResponse createCompanyAdminInvite(InviteRequest request) {
		throw new ValidationException("Invite system has been disabled. Please use POST /api/v1/admin/users/company-admin?companyId={id} instead. Companies must be created manually via POST /api/v1/admin/companies");
	}

	/**
	 * Erstellt eine Einladung für einen COMPANY_USER (nur COMPANY_ADMIN)
	 * Nutzer wird automatisch der Company des eingeloggten COMPANY_ADMIN zugeordnet
	 */
	@Transactional
	public InviteResponse createCompanyUserInvite(InviteRequest request) {
		UserPrincipal currentUser = getCurrentUser();
		validateCompanyAdmin(currentUser);

		// Prüfe ob User mit dieser Email bereits existiert
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new ValidationException("User with email " + request.getEmail() + " already exists");
		}

		// Hole Company des eingeloggten Users
		Company company = companyRepository.findById(currentUser.getCompanyId())
			.orElseThrow(() -> new ResourceNotFoundException("Company not found"));

		// Prüfe ob bereits eine ausstehende Einladung für diese Email in dieser Company existiert
		if (inviteRepository.existsByEmailAndCompanyIdAndStatus(
			request.getEmail(), company.getId(), Invite.InviteStatus.PENDING)) {
			throw new ValidationException("Pending invite already exists for email " + request.getEmail());
		}

		// Erstelle Invite
		Invite invite = createInvite(request.getEmail(), Role.COMPANY_USER, company, currentUser.getId());
		Invite savedInvite = inviteRepository.save(invite);

		return mapToResponse(savedInvite);
	}

	/**
	 * Validiert einen Invite-Token
	 */
	@Transactional(readOnly = true)
	public InviteResponse validateToken(String token) {
		Invite invite = inviteRepository.findByTokenAndStatus(token, Invite.InviteStatus.PENDING)
			.orElseThrow(() -> new ValidationException("Invalid or expired invite token"));

		if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
			invite.setStatus(Invite.InviteStatus.EXPIRED);
			inviteRepository.save(invite);
			throw new ValidationException("Invite token has expired");
		}

		return mapToResponse(invite);
	}

	/**
	 * Akzeptiert eine Einladung und erstellt den User mit Passwort
	 */
	@Transactional
	public void acceptInvite(PasswordSetupRequest request) {
		Invite invite = inviteRepository.findByTokenAndStatus(request.getToken(), Invite.InviteStatus.PENDING)
			.orElseThrow(() -> new ValidationException("Invalid or expired invite token"));

		// Prüfe Ablaufzeit
		if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
			invite.setStatus(Invite.InviteStatus.EXPIRED);
			inviteRepository.save(invite);
			throw new ValidationException("Invite token has expired");
		}

		// Prüfe ob User bereits existiert
		if (userRepository.findByEmail(invite.getEmail()).isPresent()) {
			throw new ValidationException("User with email " + invite.getEmail() + " already exists");
		}

		// Erstelle User
		User user = new User();
		user.setEmail(invite.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setRole(invite.getRole());
		user.setCompany(invite.getCompany());
		user.setActive(true);

		userRepository.save(user);

		// Markiere Invite als akzeptiert
		invite.setStatus(Invite.InviteStatus.ACCEPTED);
		invite.setAcceptedAt(LocalDateTime.now());
		inviteRepository.save(invite);
	}

	/**
	 * Holt alle Invites für die Company des eingeloggten Users
	 */
	@Transactional(readOnly = true)
	public List<InviteResponse> findAllByCompany() {
		UserPrincipal currentUser = getCurrentUser();
		List<Invite> invites = inviteRepository.findByCompanyId(currentUser.getCompanyId());
		return invites.stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	/**
	 * Holt alle Invites (nur SYSTEM_ADMIN)
	 */
	@Transactional(readOnly = true)
	public List<InviteResponse> findAll() {
		UserPrincipal currentUser = getCurrentUser();
		validateSystemAdmin(currentUser);
		return inviteRepository.findAll().stream()
			.map(this::mapToResponse)
			.collect(Collectors.toList());
	}

	private Invite createInvite(String email, Role role, Company company, Long createdBy) {
		Invite invite = new Invite();
		invite.setEmail(email);
		invite.setRole(role);
		invite.setCompany(company);
		invite.setStatus(Invite.InviteStatus.PENDING);
		invite.setToken(generateSecureToken());
		invite.setExpiresAt(LocalDateTime.now().plusDays(INVITE_VALIDITY_DAYS));
		invite.setCreatedBy(createdBy);
		return invite;
	}

	private String generateSecureToken() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[TOKEN_LENGTH];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/**
	 * @deprecated Automatische Company-Erstellung wurde entfernt.
	 * Companies müssen manuell über CompanyService erstellt werden.
	 */
	@Deprecated
	private Company findOrCreateCompany(String companyName) {
		throw new ValidationException("Automatic company creation has been disabled. Companies must be created manually via POST /api/v1/admin/companies");
	}

	private UserPrincipal getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (UserPrincipal) authentication.getPrincipal();
	}

	private void validateSystemAdmin(UserPrincipal user) {
		if (user.getRole() != Role.SYSTEM_ADMIN) {
			throw new ValidationException("Only SYSTEM_ADMIN can perform this action");
		}
	}

	private void validateCompanyAdmin(UserPrincipal user) {
		if (user.getRole() != Role.COMPANY_ADMIN) {
			throw new ValidationException("Only COMPANY_ADMIN can perform this action");
		}
	}

	private InviteResponse mapToResponse(Invite invite) {
		return new InviteResponse(
			invite.getId(),
			invite.getEmail(),
			invite.getRole().name(),
			invite.getCompany().getId(),
			invite.getCompany().getName(),
			invite.getToken(),
			invite.getStatus(),
			invite.getExpiresAt(),
			invite.getCreatedBy(),
			invite.getAcceptedAt(),
			invite.getCreatedAt(),
			invite.getUpdatedAt()
		);
	}
}

