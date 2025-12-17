# Task List: Authentication & Administration Foundation

## Relevant Files

- `backend/build.gradle` - Gradle build file with dependencies for Spring Security, JPA, PostgreSQL, and JWT
- `backend/src/main/resources/application.properties` - Application configuration including database connection, JWT settings, and initial admin credentials
- `backend/src/main/java/com/changeready/entity/Company.java` - Company entity with JPA annotations
- `backend/src/main/java/com/changeready/entity/User.java` - User entity with JPA annotations and company relationship
- `backend/src/main/java/com/changeready/entity/Role.java` - Enum for user roles (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER)
- `backend/src/main/java/com/changeready/repository/CompanyRepository.java` - Spring Data JPA repository for Company entity
- `backend/src/main/java/com/changeready/repository/UserRepository.java` - Spring Data JPA repository for User entity
- `backend/src/main/java/com/changeready/config/SecurityConfig.java` - Spring Security configuration with JWT filter chain
- `backend/src/main/java/com/changeready/security/JwtTokenProvider.java` - Utility class for JWT token generation and validation
- `backend/src/main/java/com/changeready/security/JwtAuthenticationFilter.java` - Filter for JWT token validation in request chain
- `backend/src/main/java/com/changeready/security/UserPrincipal.java` - Custom UserDetails implementation for Spring Security
- `backend/src/main/java/com/changeready/dto/auth/LoginRequest.java` - DTO for login request
- `backend/src/main/java/com/changeready/dto/auth/LoginResponse.java` - DTO for login response with JWT token
- `backend/src/main/java/com/changeready/dto/auth/LogoutRequest.java` - DTO for logout request (optional)
- `backend/src/main/java/com/changeready/dto/company/CompanyRequest.java` - DTO for company creation/update requests
- `backend/src/main/java/com/changeready/dto/company/CompanyResponse.java` - DTO for company response
- `backend/src/main/java/com/changeready/dto/user/UserRequest.java` - DTO for user creation/update requests
- `backend/src/main/java/com/changeready/dto/user/UserResponse.java` - DTO for user response (without password)
- `backend/src/main/java/com/changeready/dto/error/ErrorResponse.java` - Standardized error response DTO
- `backend/src/main/java/com/changeready/service/AuthService.java` - Service for authentication logic (login, logout)
- `backend/src/main/java/com/changeready/service/CompanyService.java` - Service for company management logic
- `backend/src/main/java/com/changeready/service/UserService.java` - Service for user management logic
- `backend/src/main/java/com/changeready/controller/AuthController.java` - REST controller for authentication endpoints
- `backend/src/main/java/com/changeready/controller/CompanyController.java` - REST controller for company management endpoints
- `backend/src/main/java/com/changeready/controller/UserController.java` - REST controller for user management endpoints
- `backend/src/main/java/com/changeready/exception/GlobalExceptionHandler.java` - Centralized exception handler with @ControllerAdvice
- `backend/src/main/java/com/changeready/exception/ResourceNotFoundException.java` - Custom exception for resource not found
- `backend/src/main/java/com/changeready/exception/UnauthorizedException.java` - Custom exception for unauthorized access
- `backend/src/main/java/com/changeready/exception/ValidationException.java` - Custom exception for validation errors
- `backend/src/main/java/com/changeready/config/InitialAdminSetup.java` - CommandLineRunner for creating initial SYSTEM_ADMIN user
- `docker-compose.yml` - Docker Compose configuration (may need PostgreSQL service added)

### Notes

- All entity classes should use Lombok annotations (@Entity, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)
- All DTOs should use validation annotations (@NotNull, @NotBlank, @Email, @Size, etc.)
- Service classes should contain all business logic; controllers should only handle HTTP concerns
- Repository interfaces extend JpaRepository and can include custom query methods
- Security configuration should disable default Spring Security login form and configure JWT filter
- Error responses should follow standardized format: { "error": "...", "code": "...", "timestamp": "..." }

## Tasks

- [x] 1.0 Setup Dependencies and Database Configuration
  - [x] 1.1 Add Spring Security dependency to build.gradle
  - [x] 1.2 Add Spring Data JPA dependency to build.gradle
  - [x] 1.3 Add PostgreSQL driver dependency to build.gradle
  - [x] 1.4 Add JWT library dependency (io.jsonwebtoken:jjwt) to build.gradle
  - [x] 1.5 Configure PostgreSQL database connection in application.properties
  - [x] 1.6 Add JWT configuration properties (secret key, expiration time) to application.properties
  - [x] 1.7 Add initial admin credentials configuration properties to application.properties
  - [x] 1.8 Update docker-compose.yml to include PostgreSQL service (if not already present)

- [x] 2.0 Create Entity Models and Repositories
  - [x] 2.1 Create Role enum (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER) in entity package
  - [x] 2.2 Create Company entity with fields: id, name, active, createdAt, updatedAt
  - [x] 2.3 Add JPA annotations to Company entity (@Entity, @Table, @Id, @GeneratedValue, etc.)
  - [x] 2.4 Add unique constraint on Company.name field
  - [x] 2.5 Create User entity with fields: id, email, passwordHash, role, company, active, createdAt, updatedAt
  - [x] 2.6 Add JPA annotations to User entity (@Entity, @Table, @Id, @GeneratedValue, etc.)
  - [x] 2.7 Add @ManyToOne relationship from User to Company with foreign key constraint
  - [x] 2.8 Add unique constraint on User.email field
  - [x] 2.9 Create CompanyRepository interface extending JpaRepository
  - [x] 2.10 Create UserRepository interface extending JpaRepository
  - [x] 2.11 Add custom query method findByEmail in UserRepository
  - [x] 2.12 Add custom query method findByCompanyId in UserRepository

- [x] 3.0 Implement Security Configuration and JWT Infrastructure
  - [x] 3.1 Create SecurityConfig class with @Configuration and @EnableWebSecurity annotations
  - [x] 3.2 Configure PasswordEncoder bean (BCryptPasswordEncoder) in SecurityConfig
  - [x] 3.3 Disable default Spring Security form login and CSRF for stateless JWT
  - [x] 3.4 Configure security filter chain to allow /api/v1/auth/** endpoints without authentication
  - [x] 3.5 Configure security filter chain to require authentication for /api/v1/admin/** endpoints
  - [x] 3.6 Create JwtTokenProvider class with methods: generateToken, validateToken, getUsernameFromToken, getRoleFromToken, getCompanyIdFromToken
  - [x] 3.7 Inject JWT secret and expiration from application.properties into JwtTokenProvider
  - [x] 3.8 Create JwtAuthenticationFilter class extending OncePerRequestFilter
  - [x] 3.9 Implement doFilterInternal in JwtAuthenticationFilter to extract and validate JWT tokens
  - [x] 3.10 Create UserPrincipal class implementing UserDetails for Spring Security integration
  - [x] 3.11 Configure JwtAuthenticationFilter in SecurityConfig filter chain before UsernamePasswordAuthenticationFilter
  - [x] 3.12 Create custom AuthenticationEntryPoint for 401 responses
  - [x] 3.13 Create custom AccessDeniedHandler for 403 responses

- [x] 4.0 Implement Authentication Service and Controllers
  - [x] 4.1 Create LoginRequest DTO with email and password fields and validation annotations
  - [x] 4.2 Create LoginResponse DTO with token, tokenType, and userInfo fields
  - [x] 4.3 Create AuthService interface with login and logout methods
  - [x] 4.4 Implement AuthService.login method: validate credentials, check user/company active status, generate JWT token
  - [x] 4.5 Implement AuthService.logout method (client-side token removal; optional server-side blacklist)
  - [x] 4.6 Add password validation utility method in AuthService (min 8 chars, uppercase, lowercase, number)
  - [x] 4.7 Create AuthController with @RestController and @RequestMapping("/api/v1/auth")
  - [x] 4.8 Implement POST /api/v1/auth/login endpoint in AuthController
  - [x] 4.9 Implement POST /api/v1/auth/logout endpoint in AuthController
  - [x] 4.10 Add proper HTTP status codes and error handling in AuthController

- [x] 5.0 Implement Company Management Service and Controllers
  - [x] 5.1 Create CompanyRequest DTO with name and active fields and validation annotations
  - [x] 5.2 Create CompanyResponse DTO with id, name, active, createdAt, updatedAt fields
  - [x] 5.3 Create CompanyService interface with create, findAll, findById, update methods
  - [x] 5.4 Implement CompanyService.create method: validate unique name, create and save company
  - [x] 5.5 Implement CompanyService.findAll method: return all companies (for SYSTEM_ADMIN)
  - [x] 5.6 Implement CompanyService.findById method: find company by ID
  - [x] 5.7 Implement CompanyService.update method: update company name and/or active status
  - [x] 5.8 Create CompanyController with @RestController and @RequestMapping("/api/v1/admin/companies")
  - [x] 5.9 Add @PreAuthorize("hasRole('SYSTEM_ADMIN')") to all CompanyController methods
  - [x] 5.10 Implement POST /api/v1/admin/companies endpoint in CompanyController
  - [x] 5.11 Implement GET /api/v1/admin/companies endpoint in CompanyController
  - [x] 5.12 Implement GET /api/v1/admin/companies/{id} endpoint in CompanyController
  - [x] 5.13 Implement PUT /api/v1/admin/companies/{id} endpoint in CompanyController
  - [x] 5.14 Add proper HTTP status codes and error handling in CompanyController

- [x] 6.0 Implement User Management Service and Controllers
  - [x] 6.1 Create UserRequest DTO with email, password, role, active fields and validation annotations
  - [x] 6.2 Create UserResponse DTO with id, email, role, companyId, active, createdAt, updatedAt fields (no password)
  - [x] 6.3 Create UserService interface with create, findAllByCompany, findById, update methods
  - [x] 6.4 Implement UserService.create method: validate unique email, hash password, ensure company is active, prevent COMPANY_ADMIN from creating SYSTEM_ADMIN
  - [x] 6.5 Implement UserService.findAllByCompany method: return all users for authenticated user's company
  - [x] 6.6 Implement UserService.findById method: find user by ID with company isolation check
  - [x] 6.7 Implement UserService.update method: update user email, password (if provided), role, active status with company isolation
  - [x] 6.8 Create UserController with @RestController and @RequestMapping("/api/v1/admin/users")
  - [x] 6.9 Add @PreAuthorize("hasRole('COMPANY_ADMIN')") to all UserController methods
  - [x] 6.10 Implement POST /api/v1/admin/users endpoint in UserController
  - [x] 6.11 Implement GET /api/v1/admin/users endpoint in UserController (returns users for authenticated user's company)
  - [x] 6.12 Implement GET /api/v1/admin/users/{id} endpoint in UserController with company isolation
  - [x] 6.13 Implement PUT /api/v1/admin/users/{id} endpoint in UserController with company isolation
  - [x] 6.14 Add proper HTTP status codes and error handling in UserController

- [x] 7.0 Implement Centralized Error Handling
  - [x] 7.1 Create ErrorResponse DTO with error, code, and timestamp fields
  - [x] 7.2 Create ResourceNotFoundException custom exception class
  - [x] 7.3 Create UnauthorizedException custom exception class
  - [x] 7.4 Create ValidationException custom exception class
  - [x] 7.5 Create GlobalExceptionHandler class with @ControllerAdvice annotation
  - [x] 7.6 Add @ExceptionHandler for ResourceNotFoundException returning 404 status
  - [x] 7.7 Add @ExceptionHandler for UnauthorizedException returning 401 status
  - [x] 7.8 Add @ExceptionHandler for ValidationException returning 400 status
  - [x] 7.9 Add @ExceptionHandler for MethodArgumentNotValidException (validation errors) returning 400 status
  - [x] 7.10 Add @ExceptionHandler for generic Exception returning 500 status
  - [x] 7.11 Ensure all exception handlers return standardized ErrorResponse format

- [x] 8.0 Implement Initial Admin Setup
  - [x] 8.1 Create InitialAdminSetup class implementing CommandLineRunner
  - [x] 8.2 Inject UserRepository and PasswordEncoder into InitialAdminSetup
  - [x] 8.3 Read initial admin email and password from application.properties
  - [x] 8.4 Check if SYSTEM_ADMIN user already exists in InitialAdminSetup.run method
  - [x] 8.5 Create initial SYSTEM_ADMIN user with default company (or create a default company) if no SYSTEM_ADMIN exists
  - [x] 8.6 Hash password before saving initial admin user
  - [x] 8.7 Add logging to indicate initial admin creation or skip

