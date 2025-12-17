package com.changeready.config;

import com.changeready.entity.Company;
import com.changeready.entity.Role;
import com.changeready.entity.User;
import com.changeready.repository.CompanyRepository;
import com.changeready.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitialAdminSetup implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(InitialAdminSetup.class);

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${admin.initial.email}")
	private String initialAdminEmail;

	@Value("${admin.initial.password}")
	private String initialAdminPassword;

	public InitialAdminSetup(
		UserRepository userRepository,
		CompanyRepository companyRepository,
		PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		// Check if SYSTEM_ADMIN user already exists
		boolean systemAdminExists = userRepository.findAll().stream()
			.anyMatch(user -> user.getRole() == Role.SYSTEM_ADMIN);

		if (systemAdminExists) {
			logger.info("SYSTEM_ADMIN user already exists. Skipping initial admin setup.");
			return;
		}

		logger.info("No SYSTEM_ADMIN user found. Creating initial admin user...");

		// Create default company for system admin
		Company defaultCompany = new Company();
		defaultCompany.setName("System Administration");
		defaultCompany.setActive(true);
		Company savedCompany = companyRepository.save(defaultCompany);
		logger.info("Created default company: {}", savedCompany.getName());

		// Create initial SYSTEM_ADMIN user
		User adminUser = new User();
		adminUser.setEmail(initialAdminEmail);
		adminUser.setPasswordHash(passwordEncoder.encode(initialAdminPassword));
		adminUser.setRole(Role.SYSTEM_ADMIN);
		adminUser.setCompany(savedCompany);
		adminUser.setActive(true);

		User savedUser = userRepository.save(adminUser);
		logger.info("Initial SYSTEM_ADMIN user created successfully with email: {}", savedUser.getEmail());
		logger.warn("Please change the default admin password after first login!");
	}
}

