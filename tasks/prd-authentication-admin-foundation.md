# Product Requirements Document: Authentication & Administration Foundation

## Introduction/Overview

This PRD defines the foundation for authentication and administration functionality in the ChangeReady360 B2B web application. The feature establishes a secure, multi-tenant authentication system where companies have accounts and users log in to access company-specific resources. This foundation will serve as the base for future survey and participation features.

**Problem Statement:** The application currently lacks any authentication or authorization mechanisms. Without this foundation, there is no way to:
- Securely identify users
- Manage company accounts and their users
- Enforce access control and tenant separation
- Provide administrative capabilities

**Goal:** Implement a robust, secure authentication and administration foundation that enables company-based multi-tenancy, role-based access control, and administrative user/company management capabilities.

## Goals

1. Enable secure user authentication via email and password with JWT-based token management
2. Implement multi-tenant architecture where users can only access resources belonging to their company
3. Provide role-based access control with three distinct roles: SYSTEM_ADMIN, COMPANY_ADMIN, and COMPANY_USER
4. Enable system administrators to create and manage companies
5. Enable company administrators to manage users within their own company
6. Ensure secure password storage using industry-standard hashing algorithms
7. Establish a clean, layered architecture following Spring Boot best practices
8. Provide a standardized error handling mechanism for consistent API responses

## User Stories

1. **As a System Administrator**, I want to create new companies so that organizations can use the platform.
2. **As a System Administrator**, I want to activate or deactivate companies so that I can control platform access.
3. **As a System Administrator**, I want to view all companies and their status so that I can manage the platform effectively.
4. **As a Company Administrator**, I want to create new users within my company so that team members can access the platform.
5. **As a Company Administrator**, I want to activate or deactivate users in my company so that I can manage team access.
6. **As a Company Administrator**, I want to view all users in my company so that I can oversee team membership.
7. **As a User**, I want to log in with my email and password so that I can access the platform securely.
8. **As a User**, I want to log out so that I can securely end my session.
9. **As a User**, I want to access only resources belonging to my company so that data is properly isolated between organizations.
10. **As a User**, I want to be prevented from logging in if my company is deactivated so that inactive companies cannot access the platform.

## Functional Requirements

### Authentication Requirements

1. The system must provide a login endpoint (`POST /api/v1/auth/login`) that accepts email and password credentials.
2. The system must validate user credentials against stored password hashes.
3. The system must generate and return a JWT token upon successful authentication.
4. The system must include user role and company ID in the JWT token payload.
5. The system must provide a logout endpoint (`POST /api/v1/auth/logout`) that invalidates the current token (client-side token removal; server-side token blacklisting is optional for this phase).
6. The system must hash passwords using BCrypt or Argon2 before storage.
7. The system must enforce password requirements: minimum 8 characters, at least one uppercase letter, at least one lowercase letter, and at least one number.
8. The system must prevent login for users whose company is deactivated, even if the user account is active.
9. The system must prevent login for deactivated user accounts.
10. The system must return appropriate error messages for invalid credentials without revealing whether the email exists.

### Company Management Requirements

11. The system must provide an endpoint to create a new company (`POST /api/v1/admin/companies`) accessible only to SYSTEM_ADMIN users.
12. The system must provide an endpoint to retrieve all companies (`GET /api/v1/admin/companies`) accessible only to SYSTEM_ADMIN users.
13. The system must provide an endpoint to retrieve a specific company by ID (`GET /api/v1/admin/companies/{id}`) accessible only to SYSTEM_ADMIN users.
14. The system must provide an endpoint to update a company (`PUT /api/v1/admin/companies/{id}`) accessible only to SYSTEM_ADMIN users.
15. The system must allow setting a company's active status (active/inactive).
16. The system must store company information including: name, active status, creation timestamp, and last modification timestamp.
17. The system must validate that company names are unique.
18. The system must enforce tenant isolation: users can only access resources belonging to their own company.

### User Management Requirements

19. The system must provide an endpoint to create a new user (`POST /api/v1/admin/users`) accessible only to COMPANY_ADMIN users within their own company.
20. The system must provide an endpoint to retrieve all users in the authenticated user's company (`GET /api/v1/admin/users`) accessible only to COMPANY_ADMIN users.
21. The system must provide an endpoint to retrieve a specific user by ID (`GET /api/v1/admin/users/{id}`) accessible only to COMPANY_ADMIN users within the same company.
22. The system must provide an endpoint to update a user (`PUT /api/v1/admin/users/{id}`) accessible only to COMPANY_ADMIN users within the same company.
23. The system must allow setting a user's active status (active/inactive).
24. The system must allow assigning roles to users: SYSTEM_ADMIN, COMPANY_ADMIN, or COMPANY_USER.
25. The system must store user information including: email, hashed password, role, company association, active status, creation timestamp, and last modification timestamp.
26. The system must validate that email addresses are unique within the system.
27. The system must ensure that COMPANY_ADMIN users can only manage users within their own company.
28. The system must prevent COMPANY_ADMIN users from creating SYSTEM_ADMIN users.
29. The system must prevent users from being created for deactivated companies.

### Authorization & Security Requirements

30. The system must protect all admin endpoints with JWT-based authentication.
31. The system must enforce role-based access control (RBAC) using Spring Security.
32. The system must extract company ID from the JWT token to enforce tenant isolation.
33. The system must return 401 Unauthorized for requests without valid JWT tokens.
34. The system must return 403 Forbidden for requests from users without required roles.
35. The system must implement a centralized exception handler (`@ControllerAdvice`) for consistent error responses.
36. The system must return standardized error response format: `{ "error": "error message", "code": "ERROR_CODE", "timestamp": "ISO-8601 timestamp" }`.

### Initial Setup Requirements

37. The system must automatically create an initial SYSTEM_ADMIN user on first application startup using a CommandLineRunner.
38. The initial SYSTEM_ADMIN user must have configurable default credentials (email and password) via application properties.
39. The system must skip initial admin creation if a SYSTEM_ADMIN user already exists.

### API Structure Requirements

40. All API endpoints must be prefixed with `/api/v1/`.
41. Authentication endpoints must be under `/api/v1/auth/`.
42. Administrative endpoints must be under `/api/v1/admin/`.
43. The system must use DTOs (Data Transfer Objects) for all request and response bodies.
44. The system must not expose entity classes directly in API responses.

### Architecture Requirements

45. The system must follow a layered architecture: Controller → Service → Repository.
46. Controllers must not contain business logic; all business logic must reside in service classes.
47. The system must use Spring Data JPA repositories for database access.
48. The system must use PostgreSQL as the database.
49. The system must organize code in clear package structure: `com.changeready.controller`, `com.changeready.service`, `com.changeready.repository`, `com.changeready.dto`, `com.changeready.entity`, `com.changeready.config`, `com.changeready.security`, `com.changeready.exception`.

## Non-Goals (Out of Scope)

The following features are explicitly **not** part of this PRD and should be ignored:

1. **Survey Functionality:** No survey creation, management, or participation features.
2. **Public Endpoints:** No public-facing endpoints for survey participation or token-based access.
3. **Participation Links:** No generation or management of survey participation links or tokens.
4. **Survey Results:** No collection, storage, or display of survey responses or results.
5. **User Self-Registration:** Users cannot register themselves; only COMPANY_ADMIN users can create users.
6. **Company Self-Registration:** Companies cannot register themselves; only SYSTEM_ADMIN users can create companies.
7. **Password Reset:** Password reset functionality is out of scope for this phase.
8. **Email Verification:** Email verification for new users is out of scope.
9. **Two-Factor Authentication (2FA):** 2FA is not included in this phase.
10. **User Profile Management:** Users cannot edit their own profiles (name, email, etc.) in this phase.
11. **Audit Logging:** Detailed audit logs of user actions are out of scope.
12. **Token Refresh:** JWT token refresh mechanism is optional for this phase (can be added later).

## Design Considerations

### API Design
- RESTful API design principles
- Consistent naming conventions (camelCase for JSON properties)
- HTTP status codes: 200 (success), 201 (created), 400 (bad request), 401 (unauthorized), 403 (forbidden), 404 (not found), 500 (server error)

### Security Design
- JWT tokens should have a reasonable expiration time (e.g., 24 hours)
- Passwords must never be logged or returned in API responses
- Use HTTPS in production (handled by infrastructure/deployment)

### Database Design
- Use appropriate indexes on frequently queried fields (email, company_id)
- Use soft deletes or active/inactive flags rather than hard deletes
- Include audit fields (created_at, updated_at) on all entities

## Technical Considerations

### Dependencies
- Spring Boot 4.0.0
- Spring Security (for authentication and authorization)
- Spring Data JPA (for database access)
- PostgreSQL JDBC Driver
- JWT library (e.g., `io.jsonwebtoken:jjwt`)
- BCrypt or Argon2 for password hashing (via Spring Security)
- Lombok (already present) for reducing boilerplate code

### Database Schema
- `companies` table: id, name, active, created_at, updated_at
- `users` table: id, email, password_hash, role, company_id (FK), active, created_at, updated_at
- Foreign key constraint: users.company_id → companies.id
- Unique constraint: companies.name, users.email

### Configuration
- JWT secret key should be configurable via `application.properties`
- Initial admin credentials should be configurable via `application.properties`
- Database connection settings for PostgreSQL

### Integration Points
- Spring Security filter chain for JWT validation
- Custom authentication provider or filter for JWT processing
- Method-level security annotations (`@PreAuthorize`) for role-based access control

## Success Metrics

1. **Security:** All admin endpoints return 401 Unauthorized when accessed without valid JWT tokens.
2. **Authorization:** COMPANY_ADMIN users can only access users within their own company.
3. **Tenant Isolation:** Users cannot access resources belonging to other companies.
4. **Functionality:** System administrators can successfully create and manage companies.
5. **Functionality:** Company administrators can successfully create and manage users within their company.
6. **Authentication:** Users can successfully log in and receive valid JWT tokens.
7. **Password Security:** Passwords are stored as hashes and cannot be retrieved in plain text.
8. **Error Handling:** All errors return standardized error response format.

## Open Questions

1. Should JWT tokens be stored in a blacklist/whitelist for logout functionality, or is client-side token removal sufficient for this phase?
2. What should be the default expiration time for JWT tokens? (Suggested: 24 hours)
3. Should there be a maximum number of users per company? (Suggested: No limit for this phase)
4. Should there be a maximum number of companies? (Suggested: No limit for this phase)
5. What should happen to existing JWT tokens when a user's role is changed? (Suggested: Tokens remain valid until expiration; role changes take effect on next login)
6. Should company deactivation immediately invalidate all active user sessions, or only prevent new logins? (Based on requirement #9: Users remain active but cannot log in, so existing tokens remain valid until expiration)

