# Technische Dokumentation: Authentication & Administration Foundation

## Inhaltsverzeichnis

1. [Architektur-Übersicht](#architektur-übersicht)
2. [Schichten-Architektur](#schichten-architektur)
3. [Security-Architektur](#security-architektur)
4. [Datenmodell](#datenmodell)
5. [Detaillierte Klassen-Beschreibungen](#detaillierte-klassen-beschreibungen)
6. [Request-Flow](#request-flow)
7. [Abhängigkeiten und Interaktionen](#abhängigkeiten-und-interaktionen)
8. [Design-Entscheidungen](#design-entscheidungen)

---

## Architektur-Übersicht

Die Authentication & Administration Foundation implementiert eine **Multi-Tenant B2B-Webanwendung** mit JWT-basierter Authentifizierung und rollenbasierter Autorisierung. Die Architektur folgt dem **Layered Architecture Pattern** mit klarer Trennung der Verantwortlichkeiten:

```
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  (HTTP Requests/Responses, Validation, Error Handling)      │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                             │
│  (Business Logic, Authorization Checks, Data Validation)   │
├─────────────────────────────────────────────────────────────┤
│                    Repository Layer                          │
│  (Database Access, JPA Queries)                            │
├─────────────────────────────────────────────────────────────┤
│                    Entity Layer                              │
│  (Domain Models, JPA Mappings)                              │
└─────────────────────────────────────────────────────────────┘
```

### Technologie-Stack

- **Spring Boot 4.0.0** - Application Framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database Access Layer
- **PostgreSQL** - Relational Database
- **JWT (jjwt)** - Token-basierte Authentifizierung
- **BCrypt** - Password Hashing
- **Lombok** - Code Reduction
- **Jakarta Validation** - Input Validation

---

## Schichten-Architektur

### 1. Entity Layer (`com.changeready.entity`)

Die Entity-Schicht definiert die Domain-Modelle und deren Beziehungen zur Datenbank.

#### `Role` (Enum)

Definiert die drei Rollen im System:

```java
public enum Role {
    SYSTEM_ADMIN,    // Kann Companies verwalten
    COMPANY_ADMIN,   // Kann Users in eigener Company verwalten
    COMPANY_USER     // Standard-User (aktuell nur für zukünftige Features)
}
```

**Verwendung:** Wird in der `User`-Entity gespeichert und für Rollen-basierte Autorisierung verwendet.

#### `Company` (Entity)

Repräsentiert eine Firma/Organisation im System.

**Wichtige Eigenschaften:**
- `@Entity` - JPA Entity Annotation
- `@Table` mit Unique Constraint auf `name`
- Automatisches Timestamp-Management via `@PrePersist` und `@PreUpdate`

```java
@Entity
@Table(name = "companies", uniqueConstraints = {
    @UniqueConstraint(name = "uk_company_name", columnNames = "name")
})
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private Boolean active = true;  // Kann deaktiviert werden
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Design-Entscheidung:** 
- `active` Flag statt Hard-Delete für Audit-Zwecke
- Automatisches Timestamp-Management reduziert Boilerplate-Code
- Unique Constraint auf Datenbank-Ebene für Datenintegrität

#### `User` (Entity)

Repräsentiert einen Benutzer im System.

**Wichtige Eigenschaften:**
- `@ManyToOne` Beziehung zu `Company` (Lazy Loading)
- Passwort wird als Hash gespeichert (`passwordHash`)
- Unique Constraint auf `email`

```java
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;  // Nie als Plain-Text!
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_user_company"))
    private Company company;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    // Timestamps wie bei Company...
}
```

**Design-Entscheidungen:**
- `FetchType.LAZY` für Company-Loading (Performance-Optimierung)
- Foreign Key Constraint auf Datenbank-Ebene
- Passwort-Hash statt Plain-Text für Sicherheit

### 2. Repository Layer (`com.changeready.repository`)

Spring Data JPA Repositories für Datenbankzugriffe.

#### `CompanyRepository`

```java
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
}
```

**Verwendung:**
- Standard CRUD-Operationen via `JpaRepository`
- Custom Query-Methode für Name-Lookup (Unique-Validierung)

#### `UserRepository`

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByCompanyId(Long companyId);
    boolean existsByEmail(String email);
}
```

**Verwendung:**
- `findByEmail` - Für Login und User-Lookup
- `findByCompanyId` - Für Company-Isolation (alle Users einer Company)
- `existsByEmail` - Für E-Mail-Unique-Validierung

**Design-Entscheidung:** 
- Method-Namen folgen Spring Data JPA Naming Convention
- Automatische Query-Generierung durch Spring Data JPA

### 3. Service Layer (`com.changeready.service`)

Enthält die gesamte Business-Logik. Controllers delegieren an Services.

#### `AuthService` / `AuthServiceImpl`

**Verantwortlichkeiten:**
- User-Authentifizierung (Login)
- JWT-Token-Generierung
- Passwort-Validierung
- Logout-Verarbeitung

**Login-Flow:**

```java
@Transactional
public LoginResponse login(LoginRequest loginRequest) {
    // 1. User finden
    User user = userRepository.findByEmail(loginRequest.getEmail())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    
    // 2. User-Status prüfen
    if (!user.getActive()) {
        throw new UnauthorizedException("User account is deactivated");
    }
    
    // 3. Company-Status prüfen
    if (!user.getCompany().getActive()) {
        throw new UnauthorizedException("Company account is deactivated");
    }
    
    // 4. Authentifizierung via Spring Security
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()
        )
    );
    
    // 5. JWT-Token generieren
    String token = tokenProvider.generateToken(authentication);
    
    // 6. Response bauen
    return new LoginResponse(token, "Bearer", userInfo);
}
```

**Passwort-Validierung:**

```java
public boolean validatePassword(String password) {
    if (password == null || password.length() < 8) {
        return false;
    }
    
    boolean hasUpperCase = false;
    boolean hasLowerCase = false;
    boolean hasDigit = false;
    
    for (char c : password.toCharArray()) {
        if (Character.isUpperCase(c)) hasUpperCase = true;
        else if (Character.isLowerCase(c)) hasLowerCase = true;
        else if (Character.isDigit(c)) hasDigit = true;
    }
    
    return hasUpperCase && hasLowerCase && hasDigit;
}
```

**Design-Entscheidungen:**
- `@Transactional` für Datenkonsistenz
- Generische Fehlermeldung bei ungültigen Credentials (Security Best Practice)
- Passwort wird nie im Log ausgegeben

#### `CompanyService` / `CompanyServiceImpl`

**Verantwortlichkeiten:**
- Company CRUD-Operationen
- Unique-Name-Validierung
- Nur für SYSTEM_ADMIN zugänglich

**Create-Methode:**

```java
@Transactional
public CompanyResponse create(CompanyRequest request) {
    // Unique-Name-Validierung
    if (companyRepository.findByName(request.getName()).isPresent()) {
        throw new ValidationException(
            "Company with name '" + request.getName() + "' already exists"
        );
    }
    
    Company company = new Company();
    company.setName(request.getName());
    company.setActive(request.getActive() != null ? request.getActive() : true);
    
    Company savedCompany = companyRepository.save(company);
    return mapToResponse(savedCompany);
}
```

**Design-Entscheidung:**
- Entity zu DTO Mapping in Service-Layer (Controller bleibt schlank)
- Validierung auf Service-Ebene (zusätzlich zu DTO-Validierung)

#### `UserService` / `UserServiceImpl`

**Verantwortlichkeiten:**
- User CRUD-Operationen
- **Company-Isolation** - COMPANY_ADMIN kann nur eigene Company-Users sehen/bearbeiten
- Passwort-Hashing
- Rollen-Validierung (COMPANY_ADMIN kann keine SYSTEM_ADMIN erstellen)

**Company-Isolation Beispiel:**

```java
@Transactional(readOnly = true)
public List<UserResponse> findAllByCompany(Long companyId) {
    // Authentifizierten User holen
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
    
    // Company-Isolation prüfen
    if (currentUser.getRole() == Role.COMPANY_ADMIN 
        && !companyId.equals(currentUser.getCompanyId())) {
        throw new UnauthorizedException("Cannot access users from other companies");
    }
    
    return userRepository.findByCompanyId(companyId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
}
```

**Create-Methode mit Validierungen:**

```java
@Transactional
public UserResponse create(UserRequest request, Long companyId) {
    // 1. Rolle-Validierung
    if (currentUser.getRole() == Role.COMPANY_ADMIN 
        && request.getRole() == Role.SYSTEM_ADMIN) {
        throw new UnauthorizedException("COMPANY_ADMIN cannot create SYSTEM_ADMIN users");
    }
    
    // 2. Company-Existenz und Status prüfen
    Company company = companyRepository.findById(companyId)
        .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    
    if (!company.getActive()) {
        throw new ValidationException("Cannot create users for deactivated company");
    }
    
    // 3. Company-Isolation
    if (currentUser.getRole() == Role.COMPANY_ADMIN 
        && !companyId.equals(currentUser.getCompanyId())) {
        throw new UnauthorizedException("Cannot create users for other companies");
    }
    
    // 4. E-Mail-Unique-Validierung
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new ValidationException("Email already exists");
    }
    
    // 5. Passwort-Validierung und Hashing
    if (!authService.validatePassword(request.getPassword())) {
        throw new ValidationException("Password validation failed");
    }
    
    // 6. User erstellen
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole());
    user.setCompany(company);
    user.setActive(true);
    
    return mapToResponse(userRepository.save(user));
}
```

**Design-Entscheidungen:**
- Mehrschichtige Validierung (Rolle, Company-Status, Isolation, E-Mail-Unique)
- Passwort wird sofort gehasht (nie Plain-Text in Entity)
- Company-Isolation auf Service-Ebene (zusätzlich zu Controller-Autorisierung)

### 4. Controller Layer (`com.changeready.controller`)

REST-Controller für HTTP-Endpunkte. Enthalten **keine** Business-Logik.

#### `AuthController`

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
```

**Design-Entscheidung:**
- Controller ist schlank - delegiert an Service
- `@Valid` für automatische DTO-Validierung
- Klare HTTP-Status-Codes

#### `CompanyController`

```java
@RestController
@RequestMapping("/api/v1/admin/companies")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class CompanyController {
    
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
        @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        List<CompanyResponse> companies = companyService.findAll();
        return ResponseEntity.ok(companies);
    }
    
    // Weitere Endpoints...
}
```

**Design-Entscheidung:**
- `@PreAuthorize` auf Klassen-Ebene (gilt für alle Methoden)
- HTTP 201 für CREATE, 200 für GET

#### `UserController`

```java
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class UserController {
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        // Company-ID aus authentifiziertem User holen
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        
        UserResponse response = userService.create(
            request, 
            currentUser.getCompanyId()  // Automatische Company-Isolation
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Weitere Endpoints...
}
```

**Design-Entscheidung:**
- Company-ID wird automatisch aus JWT-Token extrahiert
- Keine Möglichkeit für COMPANY_ADMIN, andere Companies zu manipulieren

### 5. DTO Layer (`com.changeready.dto`)

Data Transfer Objects für Request/Response. **Nie** Entities direkt exponieren!

#### Request DTOs

**LoginRequest:**
```java
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
}
```

**UserRequest:**
```java
public class UserRequest {
    @NotBlank @Email
    private String email;
    
    private String password;  // Optional für Updates
    
    @NotNull
    private Role role;
    
    private Boolean active = true;
}
```

#### Response DTOs

**LoginResponse:**
```java
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserInfo userInfo;
    
    public static class UserInfo {
        private Long id;
        private String email;
        private String role;
        private Long companyId;
    }
}
```

**UserResponse:**
```java
public class UserResponse {
    private Long id;
    private String email;
    private Role role;
    private Long companyId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // KEIN passwordHash!
}
```

**Design-Entscheidung:**
- Passwort wird **nie** in Response DTOs enthalten
- Separate DTOs für Request/Response (Flexibilität)
- Validation-Annotations auf Request-DTOs

---

## Security-Architektur

### JWT-basierte Authentifizierung

Die Security-Architektur basiert auf **stateless JWT-Tokens** mit folgenden Komponenten:

#### 1. `SecurityConfig`

Konfiguriert die gesamte Security-Filter-Chain:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Stateless, kein CSRF nötig
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()  // Login öffentlich
                .requestMatchers("/api/v1/admin/**").authenticated()  // Admin geschützt
                .anyRequest().permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401 Handler
                .accessDeniedHandler(jwtAccessDeniedHandler)  // 403 Handler
            )
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }
}
```

**Design-Entscheidungen:**
- **Stateless** - Keine Server-Side Sessions (Skalierbarkeit)
- CSRF deaktiviert (nicht nötig bei stateless JWT)
- Method-Level Security aktiviert (`@PreAuthorize`)

#### 2. `JwtTokenProvider`

Generiert und validiert JWT-Tokens:

```java
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long expirationInMs;
    
    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expirationInMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationInMs = expirationInMs;
    }
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);
        
        return Jwts.builder()
            .subject(userPrincipal.getEmail())
            .claim("id", userPrincipal.getId())
            .claim("role", userPrincipal.getRole().name())
            .claim("companyId", userPrincipal.getCompanyId())  // Wichtig für Isolation!
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // Weitere Methoden zum Extrahieren von Claims...
}
```

**JWT-Token-Struktur:**
```json
{
  "sub": "user@example.com",
  "id": 1,
  "role": "COMPANY_ADMIN",
  "companyId": 5,
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Design-Entscheidungen:**
- `companyId` im Token für effiziente Company-Isolation
- Secret Key aus Properties (muss in Production geändert werden!)
- Expiration konfigurierbar (Standard: 24 Stunden)

#### 3. `JwtAuthenticationFilter`

Filtert jeden Request und extrahiert/validiert JWT-Token:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // 1. Token aus Authorization Header extrahieren
            String jwt = getJwtFromRequest(request);
            
            // 2. Token validieren
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 3. Email aus Token extrahieren
                String email = tokenProvider.getEmailFromToken(jwt);
                
                // 4. UserPrincipal laden
                UserPrincipal userPrincipal = userDetailsService.loadUserByUsername(email);
                
                // 5. User aktiv?
                if (userPrincipal != null && userPrincipal.isEnabled()) {
                    // 6. Authentication in SecurityContext setzen
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities()
                        );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " entfernen
        }
        return null;
    }
}
```

**Request-Flow:**
1. Client sendet Request mit `Authorization: Bearer <token>`
2. Filter extrahiert Token aus Header
3. Token wird validiert (Signatur, Expiration)
4. UserPrincipal wird geladen
5. Authentication wird in SecurityContext gesetzt
6. Request wird weitergeleitet

**Design-Entscheidungen:**
- Filter läuft **vor** UsernamePasswordAuthenticationFilter
- Fehler werden geloggt, aber Request wird weitergeleitet (andere Filter können noch greifen)
- Token-Format: `Bearer <token>`

#### 4. `UserPrincipal` / `UserDetailsServiceImpl`

`UserPrincipal` implementiert Spring Security's `UserDetails`:

```java
public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private Role role;
    private Long companyId;  // Wichtig für Company-Isolation!
    private Boolean active;
    private Collection<? extends GrantedAuthority> authorities;
    
    public static UserPrincipal create(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getRole(),
            user.getCompany().getId(),
            user.getActive(),
            Collections.singletonList(authority)
        );
    }
    
    @Override
    public boolean isEnabled() {
        return active;  // Deaktivierte User können sich nicht einloggen
    }
}
```

`UserDetailsServiceImpl` lädt User aus Datenbank:

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Override
    @Transactional
    public UserPrincipal loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        
        return UserPrincipal.create(user);
    }
}
```

**Design-Entscheidung:**
- `isEnabled()` prüft `active` Flag
- Authorities werden als `ROLE_<ROLE_NAME>` formatiert (Spring Security Konvention)

#### 5. Exception Handler für Security

**JwtAuthenticationEntryPoint** (401 Unauthorized):
```java
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(...) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\":\"Unauthorized\",\"code\":\"UNAUTHORIZED\"...}");
    }
}
```

**JwtAccessDeniedHandler** (403 Forbidden):
```java
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(...) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\":\"Access Denied\",\"code\":\"FORBIDDEN\"...}");
    }
}
```

---

## Request-Flow

### Login-Flow

```
1. Client → POST /api/v1/auth/login
   {
     "email": "user@example.com",
     "password": "Password123"
   }

2. AuthController.login()
   ↓
3. AuthService.login()
   ├─ UserRepository.findByEmail() → User Entity
   ├─ Prüfe: user.active == true?
   ├─ Prüfe: user.company.active == true?
   ├─ AuthenticationManager.authenticate()
   │  └─ UserDetailsServiceImpl.loadUserByUsername()
   │     └─ PasswordEncoder.matches() → BCrypt Vergleich
   ├─ JwtTokenProvider.generateToken()
   │  └─ JWT mit Claims: email, id, role, companyId
   └─ LoginResponse mit Token

4. Response → Client
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tokenType": "Bearer",
     "userInfo": { ... }
   }
```

### Geschützter Request-Flow (z.B. GET /api/v1/admin/users)

```
1. Client → GET /api/v1/admin/users
   Header: Authorization: Bearer <token>

2. JwtAuthenticationFilter.doFilterInternal()
   ├─ Extrahiere Token aus Header
   ├─ JwtTokenProvider.validateToken() → true/false
   ├─ JwtTokenProvider.getEmailFromToken()
   ├─ UserDetailsServiceImpl.loadUserByUsername()
   └─ SecurityContextHolder.setAuthentication()

3. SecurityConfig.filterChain()
   ├─ Prüfe: /api/v1/admin/** → authenticated()?
   └─ Weiterleitung zu Controller

4. UserController.getAllUsers()
   ├─ @PreAuthorize("hasRole('COMPANY_ADMIN')")
   │  └─ Prüfe: User hat ROLE_COMPANY_ADMIN?
   ├─ SecurityContextHolder.getContext().getAuthentication()
   │  └─ Extrahiere UserPrincipal mit companyId
   └─ UserService.findAllByCompany(companyId)
      ├─ Company-Isolation Check
      ├─ UserRepository.findByCompanyId()
      └─ Map zu UserResponse DTOs

5. Response → Client
   [
     {
       "id": 1,
       "email": "user1@example.com",
       "role": "COMPANY_USER",
       "companyId": 5,
       ...
     }
   ]
```

### Fehler-Flow

```
1. Exception wird geworfen (z.B. UnauthorizedException)

2. GlobalExceptionHandler.handleUnauthorizedException()
   ├─ ErrorResponse DTO erstellen
   └─ ResponseEntity mit HTTP 401

3. Response → Client
   {
     "error": "User account is deactivated",
     "code": "UNAUTHORIZED",
     "timestamp": "2024-01-01T12:00:00Z"
   }
```

---

## Abhängigkeiten und Interaktionen

### Dependency Graph

```
Controller Layer
    ↓ (verwendet)
Service Layer
    ↓ (verwendet)
Repository Layer
    ↓ (verwendet)
Entity Layer
    ↓ (mapped zu)
Database

Security Layer (parallel)
    ↓ (schützt)
Controller Layer
```

### Konkrete Abhängigkeiten

**AuthController:**
- `AuthService` → Login/Logout-Logik

**CompanyController:**
- `CompanyService` → Company CRUD-Logik

**UserController:**
- `UserService` → User CRUD-Logik
- `SecurityContextHolder` → Authentifizierten User holen

**AuthService:**
- `AuthenticationManager` → Spring Security Authentifizierung
- `JwtTokenProvider` → Token-Generierung
- `UserRepository` → User-Lookup

**UserService:**
- `UserRepository` → User CRUD
- `CompanyRepository` → Company-Validierung
- `PasswordEncoder` → Passwort-Hashing
- `AuthService` → Passwort-Validierung
- `SecurityContextHolder` → Company-Isolation

**JwtAuthenticationFilter:**
- `JwtTokenProvider` → Token-Validierung
- `UserDetailsServiceImpl` → UserPrincipal laden

---

## Design-Entscheidungen

### 1. Multi-Tenant Architecture

**Entscheidung:** Company-basierte Isolation
- Jeder User gehört zu einer Company
- COMPANY_ADMIN kann nur eigene Company-Users sehen/bearbeiten
- `companyId` im JWT-Token für effiziente Isolation

**Vorteile:**
- Klare Daten-Trennung
- Skalierbar (mehrere Companies)
- Sicherheit durch Isolation

### 2. Stateless JWT Authentication

**Entscheidung:** Keine Server-Side Sessions
- JWT-Token enthält alle nötigen Informationen
- Keine Session-Storage nötig
- Skalierbar über mehrere Server

**Vorteile:**
- Horizontal skalierbar
- Keine Session-Storage-Kosten
- Token kann auch von anderen Services validiert werden

**Nachteile:**
- Token kann nicht vor Ablauf invalidiert werden (außer Blacklist)
- Token-Größe limitiert (aktuell kein Problem)

### 3. Layered Architecture

**Entscheidung:** Klare Schicht-Trennung
- Controller → Service → Repository → Entity
- Keine Business-Logik in Controllern
- Keine Datenbank-Logik in Services

**Vorteile:**
- Testbarkeit (jede Schicht isoliert testbar)
- Wartbarkeit (klare Verantwortlichkeiten)
- Wiederverwendbarkeit

### 4. DTO Pattern

**Entscheidung:** Separate DTOs für Request/Response
- Entities werden nie direkt exponiert
- Passwort wird nie in Responses gesendet
- Flexibilität für API-Änderungen ohne Entity-Änderungen

**Vorteile:**
- Sicherheit (keine sensiblen Daten in Responses)
- API-Stabilität (Entity-Änderungen beeinflussen API nicht)
- Validierung auf DTO-Ebene

### 5. Exception Handling

**Entscheidung:** Zentralisierter Exception Handler
- `@ControllerAdvice` für globale Exception-Behandlung
- Standardisiertes Error-Response-Format
- Spezifische Exception-Typen für verschiedene Fehler

**Vorteile:**
- Konsistente Fehler-Responses
- Einfache Wartung
- Klare Fehler-Codes für Frontend

### 6. Password Security

**Entscheidung:** BCrypt Hashing
- Passwörter werden nie als Plain-Text gespeichert
- BCrypt mit automatischem Salt
- Passwort-Validierung: min. 8 Zeichen, Groß-/Kleinbuchstaben, Zahl

**Vorteile:**
- Sicherheit auch bei Datenbank-Leak
- BCrypt ist bewährter Standard
- Automatisches Salt (keine manuelle Verwaltung)

### 7. Initial Admin Setup

**Entscheidung:** CommandLineRunner für ersten Admin
- Automatische Erstellung beim ersten Start
- Konfigurierbar via Properties
- Skip wenn bereits vorhanden

**Vorteile:**
- Einfaches Setup
- Keine manuelle Datenbank-Manipulation nötig
- Idempotent (mehrfaches Starten ohne Fehler)

---

## Code-Beispiele

### Beispiel: Company-Isolation in UserService

```java
@Override
@Transactional(readOnly = true)
public UserResponse findById(Long id, Long companyId) {
    // 1. Authentifizierten User holen
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
    
    // 2. Company-Isolation prüfen
    // COMPANY_ADMIN darf nur eigene Company-Users sehen
    if (currentUser.getRole() == Role.COMPANY_ADMIN 
        && !companyId.equals(currentUser.getCompanyId())) {
        throw new UnauthorizedException("Cannot access users from other companies");
    }
    
    // 3. User finden
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    // 4. Verifizieren: User gehört zur angegebenen Company?
    if (!user.getCompany().getId().equals(companyId)) {
        throw new UnauthorizedException("User does not belong to the specified company");
    }
    
    return mapToResponse(user);
}
```

**Warum zwei Checks?**
- Erster Check: Verhindert, dass COMPANY_ADMIN andere Companies abfragt
- Zweiter Check: Verhindert, dass User aus falscher Company zurückgegeben wird (zusätzliche Sicherheit)

### Beispiel: JWT-Token-Generierung

```java
public String generateToken(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationInMs);
    
    return Jwts.builder()
        .subject(userPrincipal.getEmail())  // Standard JWT Claim
        .claim("id", userPrincipal.getId())  // Custom Claim
        .claim("role", userPrincipal.getRole().name())  // Custom Claim
        .claim("companyId", userPrincipal.getCompanyId())  // Wichtig für Isolation!
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)  // HMAC SHA-256 Signatur
        .compact();
}
```

**Warum companyId im Token?**
- Effizienz: Keine zusätzliche Datenbank-Abfrage nötig
- Isolation: Company-ID ist sofort verfügbar
- Sicherheit: Token ist signiert, kann nicht manipuliert werden

### Beispiel: Passwort-Hashing beim User-Create

```java
// ❌ FALSCH - Passwort als Plain-Text speichern
user.setPassword(request.getPassword());

// ✅ RICHTIG - Passwort hashen
user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
```

**BCrypt Hash Beispiel:**
```
Plain-Text: "Password123"
BCrypt Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
```

Der Hash enthält:
- Algorithmus (`$2a$`)
- Cost Factor (`10` - 2^10 Iterationen)
- Salt (automatisch generiert)
- Hash-Wert

---

## Zusammenfassung

Die Authentication & Administration Foundation implementiert:

✅ **Sichere Authentifizierung** mit JWT-Tokens
✅ **Multi-Tenant Architecture** mit Company-Isolation
✅ **Rollen-basierte Autorisierung** (SYSTEM_ADMIN, COMPANY_ADMIN, COMPANY_USER)
✅ **Saubere Schichten-Architektur** (Controller → Service → Repository → Entity)
✅ **Zentralisiertes Error Handling** mit standardisiertem Format
✅ **Passwort-Sicherheit** mit BCrypt Hashing
✅ **Automatisches Initial Setup** für ersten Admin-User

Die Architektur ist **skalierbar**, **wartbar** und **sicher** - eine solide Basis für zukünftige Features wie Surveys und Teilnahme-Links.

