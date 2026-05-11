# 📋 Plan de Travail Backend - BudgetSmart

## 📊 Analyse de la Conception

### Architecture Actuelle
- **Framework** : Spring Boot 3.2+
- **Langage** : Java 17+
- **Base de données** : PostgreSQL 15+
- **Authentification** : JWT
- **Assistant IA** : Claude API via n8n
- **ORM** : Spring Data JPA

### Changement Majeur
> Le bot (Claude API) est géré via **n8n**. 
> - n8n envoie une requête **POST** au backend avec la requête utilisateur
> - Le backend traite et répond à n8n
> - n8n gère l'intégration avec Claude API

---

## 🎯 Objectifs du Backend

1. ✅ Gérer l'authentification JWT
2. ✅ CRUD complet des budgets, revenus, dépenses
3. ✅ Gestion des alertes avec triggers PostgreSQL
4. ✅ Endpoint pour recevoir les requêtes POST de n8n
5. ✅ Analyse des données budgétaires
6. ✅ Sécurisation des endpoints
7. ✅ Gestion des erreurs robuste

---

## 📁 PHASE 1 : Configuration du Projet

### 1.1 Setup Initial
- [ ] Créer un projet Maven/Gradle Spring Boot
- [ ] Ajouter les dépendances :
  ```xml
  <!-- Core -->
  <spring-boot-starter-web>
  <spring-boot-starter-data-jpa>
  <spring-boot-starter-security>
  
  <!-- Database -->
  <postgresql>
  <flyway-core> <!-- Migrations SQL -->
  
  <!-- JWT -->
  <jjwt>
  
  <!-- Validation -->
  <spring-boot-starter-validation>
  
  <!-- Lombok -->
  <lombok>
  
  <!-- Tests -->
  <spring-boot-starter-test>
  ```

### 1.2 Configuration application.properties
- [ ] Configuration PostgreSQL
- [ ] Configuration JWT (secret, expiration)
- [ ] Configuration Logging
- [ ] Profils (dev, prod)
- [ ] CORS pour n8n et frontend

### 1.3 Structure des Répertoires
```
src/main/java/com/budgetsmart/
├── config/
│   ├── JwtConfig.java
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── controller/
│   ├── AuthController.java
│   ├── BudgetController.java
│   ├── ExpenseController.java
│   ├── RevenueController.java
│   ├── SavingsController.java
│   ├── AlertController.java
│   └── ChatController.java (n8n endpoint)
├── service/
│   ├── AuthService.java
│   ├── BudgetService.java
│   ├── ExpenseService.java
│   ├── RevenueService.java
│   ├── SavingsService.java
│   ├── AlertService.java
│   └── ChatService.java
├── repository/
│   ├── UserRepository.java
│   ├── BudgetRepository.java
│   ├── ExpenseRepository.java
│   ├── RevenueRepository.java
│   ├── SavingsRepository.java
│   ├── AlertRepository.java
│   └── CategoryRepository.java
├── entity/
│   ├── User.java
│   ├── Budget.java
│   ├── Expense.java
│   ├── Revenue.java
│   ├── Savings.java
│   ├── Alert.java
│   └── Category.java
├── dto/
│   ├── AuthDtos.java
│   ├── BudgetDtos.java
│   ├── ExpenseDtos.java
│   ├── ChatDtos.java
│   └── ResponseDtos.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── ValidationException.java
├── security/
│   ├── JwtProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── utils/
    ├── Constants.java
    └── Validators.java

src/main/resources/
├── db/migration/ (Flyway migrations)
│   ├── V1__initial_schema.sql
│   ├── V2__add_triggers.sql
│   └── V3__add_indexes.sql
└── application.properties
```

---

## 🔐 PHASE 2 : Authentification JWT

### 2.1 Entité User
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    private String phone;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### 2.2 JWT Provider
- [ ] Implémenter la génération de token JWT
- [ ] Implémenter la validation de token
- [ ] Implémenter le refresh token
- [ ] Gérer les exceptions JWT

### 2.3 Security Filter
- [ ] Créer JwtAuthenticationFilter
- [ ] Intercepter les requêtes (sauf endpoints publics)
- [ ] Valider le token à chaque requête
- [ ] Stocker le contexte utilisateur

### 2.4 Endpoints Authentification
```
POST   /api/auth/register          → Inscription
POST   /api/auth/login             → Connexion
POST   /api/auth/refresh           → Renouveler token
POST   /api/auth/logout            → Déconnexion
GET    /api/auth/me                → Profil utilisateur
PUT    /api/auth/profile           → Modifier profil
```

---

## 💰 PHASE 3 : Gestion des Revenus et Dépenses

### 3.1 Entités
```java
// Revenue.java
@Entity
@Table(name = "revenues")
public class Revenue {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private LocalDate revenueDate;
    private LocalDateTime createdAt;
}

// Expense.java (similaire à Revenue)
```

### 3.2 Repositories avec Queries Personnalisées
- [ ] Trouver les dépenses par période (mois, année)
- [ ] Totaliser par catégorie
- [ ] Filtrer par utilisateur et date
- [ ] Pagination et tri

```java
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdAndExpenseDateBetween(Long userId, LocalDate start, LocalDate end);
    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);
    Optional<BigDecimal> sumByUserIdAndExpenseDateBetween(Long userId, LocalDate start, LocalDate end);
}
```

### 3.3 Services
- [ ] ExpenseService
  - Créer, modifier, supprimer une dépense
  - Obtenir les dépenses du mois courant
  - Calculer le total par catégorie
  - Générer un résumé mensuel
  
- [ ] RevenueService
  - Créer, modifier, supprimer un revenu
  - Obtenir les revenus du mois courant
  - Calculer le total

### 3.4 Contrôleurs
```
POST   /api/expenses                → Créer une dépense
GET    /api/expenses                → Lister (avec filtres)
GET    /api/expenses/{id}           → Détail
PUT    /api/expenses/{id}           → Modifier
DELETE /api/expenses/{id}           → Supprimer
GET    /api/expenses/summary/{year}/{month} → Résumé

POST   /api/revenues                → Créer un revenu
GET    /api/revenues                → Lister
GET    /api/revenues/{id}           → Détail
PUT    /api/revenues/{id}           → Modifier
DELETE /api/revenues/{id}           → Supprimer
```

### 3.5 DTOs
```java
public class ExpenseCreateRequest {
    @NotNull
    private BigDecimal amount;
    
    @NotBlank
    private String description;
    
    private Long categoryId;
    private LocalDate expenseDate;
}

public class ExpenseResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private CategoryResponse category;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
}
```

---

## 🎯 PHASE 4 : Gestion des Budgets et Alertes

### 4.1 Entités
```java
// Budget.java
@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(nullable = false)
    private BigDecimal limitAmount;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;
}

// Alert.java
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String message;
    private AlertType type; // WARNING, CRITICAL
    private boolean read = false;
    private LocalDateTime createdAt;
}
```

### 4.2 Triggers PostgreSQL (Migration Flyway)
- [ ] Trigger : Vérifier le budget à chaque dépense
- [ ] Trigger : Créer une alerte si dépassement
- [ ] Trigger : Archiver les budgets expirés

```sql
-- V2__add_triggers.sql
CREATE OR REPLACE FUNCTION check_budget_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT SUM(amount) FROM expenses 
        WHERE user_id = NEW.user_id 
        AND category_id = NEW.category_id
        AND expense_date >= CURRENT_DATE) 
       > (SELECT limit_amount FROM budgets 
          WHERE user_id = NEW.user_id 
          AND category_id = NEW.category_id)
    THEN
        INSERT INTO alerts (user_id, message, type, created_at)
        VALUES (NEW.user_id, 'Budget dépassé !', 'CRITICAL', NOW());
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER budget_check AFTER INSERT ON expenses
FOR EACH ROW EXECUTE FUNCTION check_budget_trigger();
```

### 4.3 Services
- [ ] BudgetService
  - CRUD des budgets
  - Vérifier l'état du budget
  - Calculer la dépense restante

- [ ] AlertService
  - Récupérer les alertes non lues
  - Marquer comme lues
  - Supprimer les alertes

### 4.4 Contrôleurs
```
POST   /api/budgets                 → Créer budget
GET    /api/budgets                 → Lister
PUT    /api/budgets/{id}            → Modifier
DELETE /api/budgets/{id}            → Supprimer
GET    /api/budgets/{id}/status     → État du budget

GET    /api/alerts                  → Lister alertes
GET    /api/alerts/unread           → Alertes non lues
PUT    /api/alerts/{id}/read        → Marquer comme lue
DELETE /api/alerts/{id}             → Supprimer
```

---

## 💾 PHASE 5 : Gestion de l'Épargne

### 5.1 Entité Savings
```java
@Entity
@Table(name = "savings")
public class Savings {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    private LocalDate targetDate;
    private boolean completed = false;
    private LocalDateTime createdAt;
}
```

### 5.2 Services
- [ ] SavingsService
  - Créer un objectif d'épargne
  - Ajouter/retirer du montant épargné
  - Calculer le pourcentage complété
  - Vérifier si objectif atteint

### 5.3 Contrôleurs
```
POST   /api/savings                 → Créer objectif
GET    /api/savings                 → Lister
GET    /api/savings/{id}            → Détail + progrès
PUT    /api/savings/{id}            → Modifier
DELETE /api/savings/{id}            → Supprimer
POST   /api/savings/{id}/add        → Ajouter montant
```

---

## 🤖 PHASE 6 : Intégration n8n - CRITIQUE

### 6.1 Architecture du Chat
```
┌──────────────┐
│   Frontend   │
│   (React)    │
└──────┬───────┘
       │ Envoi message utilisateur
       │ GET /api/chat/message
       │
┌──────▼──────────────────────────────┐
│      Backend Spring Boot            │
│   ChatController (endpoint public)   │
└──────┬──────────────────────────────┘
       │ Appelé par n8n ou Frontend
       │ POST /api/chat/process
       │
┌──────▼──────────────────────────────┐
│          n8n Workflow               │
│  1. Reçoit requête du backend       │
│  2. Prépare le contexte utilisateur │
│  3. Appelle Claude API              │
│  4. Renvoie réponse au backend      │
└──────┬──────────────────────────────┘
       │ Réponse IA
       │
┌──────▼──────────────────────────────┐
│      Backend traite réponse         │
│   Enregistre historique             │
│   Retourne au Frontend              │
└──────────────────────────────────────┘
```

### 6.2 DTOs Chat
```java
// ChatRequest.java
public class ChatRequest {
    @NotBlank
    private String message;
    
    private LocalDateTime timestamp = LocalDateTime.now();
}

// ChatResponse.java
public class ChatResponse {
    private String response;
    private String analysis; // Analyse du budget
    private LocalDateTime timestamp;
}

// N8nWebhookPayload.java
public class N8nWebhookPayload {
    private Long userId;
    private String message;
    private Map<String, Object> userContext; // Budget, dépenses, etc.
}

// N8nWebhookResponse.java
public class N8nWebhookResponse {
    private String response;
    private boolean success;
    private String error;
}
```

### 6.3 ChatController
```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    // Endpoint pour le frontend
    @PostMapping("/message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> sendMessage(
        @RequestBody ChatRequest request,
        @AuthenticationPrincipal UserDetails user) {
        // 1. Récupérer contexte utilisateur (budget, dépenses)
        // 2. Créer payload pour n8n
        // 3. Appeler n8n webhook
        // 4. Enregistrer dans l'historique
        // 5. Retourner réponse
    }
    
    // Endpoint webhook pour n8n (sans authentification)
    @PostMapping("/webhook/n8n")
    public ResponseEntity<N8nWebhookResponse> handleN8nRequest(
        @RequestBody N8nWebhookPayload payload,
        @RequestHeader("X-N8N-API-KEY") String apiKey) {
        
        // 1. Valider la clé API n8n
        if (!validateN8nApiKey(apiKey)) {
            return ResponseEntity.status(UNAUTHORIZED).build();
        }
        
        // 2. Récupérer données utilisateur pour contexte
        User user = userService.findById(payload.getUserId());
        
        // 3. Préparer contexte (résumé budget, dépenses, alertes)
        String context = buildUserContext(user);
        
        // 4. Retourner les données à n8n (qui appellera Claude)
        return ResponseEntity.ok(new N8nWebhookResponse(
            "Context prepared for Claude",
            true,
            null
        ));
    }
}
```

### 6.4 ChatService
```java
@Service
public class ChatService {
    
    private final RestTemplate restTemplate;
    private final N8nConfig n8nConfig;
    private final UserService userService;
    private final BudgetService budgetService;
    
    // Construire contexte utilisateur pour Claude
    public String buildUserContext(User user) {
        StringBuilder context = new StringBuilder();
        
        // Ajouter info utilisateur
        context.append("Utilisateur: ").append(user.getFirstName()).append("\n");
        
        // Ajouter budget du mois
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        BigDecimal monthlyExpense = expenseService
            .getTotalByUserAndPeriod(user.getId(), firstDay, today);
        context.append("Dépenses ce mois: ").append(monthlyExpense).append("\n");
        
        // Ajouter alertes actives
        List<Alert> alerts = alertService.getUnreadAlerts(user.getId());
        if (!alerts.isEmpty()) {
            context.append("Alertes actives: ").append(alerts.size()).append("\n");
        }
        
        return context.toString();
    }
    
    // Appeler n8n webhook
    public String callN8nWorkflow(Long userId, String userMessage) {
        N8nWebhookPayload payload = new N8nWebhookPayload();
        payload.setUserId(userId);
        payload.setMessage(userMessage);
        payload.setUserContext(buildUserContextMap(userId));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-N8N-API-KEY", n8nConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<N8nWebhookPayload> request = new HttpEntity<>(payload, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                n8nConfig.getWebhookUrl(),
                request,
                String.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new ChatException("Erreur lors de l'appel à n8n", e);
        }
    }
}
```

### 6.5 Configuration n8n
```properties
# application.properties
n8n.webhook.url=http://localhost:5678/webhook/chat
n8n.api.key=your-n8n-api-key-here
n8n.timeout=30000
```

### 6.6 Endpoints Chat
```
POST   /api/chat/message            → Envoyer message (Frontend)
POST   /api/chat/webhook/n8n        → Webhook n8n (n8n)
GET    /api/chat/history            → Historique chat
DELETE /api/chat/history/{id}       → Supprimer message
```

---

## ⚠️ PHASE 7 : Gestion des Erreurs et Validation

### 7.1 Exceptions Personnalisées
```java
public class ResourceNotFoundException extends RuntimeException {}
public class UnauthorizedException extends RuntimeException {}
public class ValidationException extends RuntimeException {}
public class ChatException extends RuntimeException {}
public class DatabaseException extends RuntimeException {}
```

### 7.2 GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND)
            .body(new ErrorResponse(NOT_FOUND.value(), e.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        return ResponseEntity.status(BAD_REQUEST)
            .body(new ErrorResponse(BAD_REQUEST.value(), e.getMessage()));
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(UNAUTHORIZED)
            .body(new ErrorResponse(UNAUTHORIZED.value(), e.getMessage()));
    }
}
```

### 7.3 Validations
- [ ] Email valide
- [ ] Montants positifs
- [ ] Dates cohérentes
- [ ] Longueur des strings

---

## 🔒 PHASE 8 : Sécurité

### 8.1 Configuration Security
- [ ] CORS configuré pour n8n et Frontend
- [ ] HTTPS en production
- [ ] Rate limiting sur endpoints critiques
- [ ] Validation entrées utilisateur
- [ ] Injection SQL prévenue (JPA)

### 8.2 Sécurité des Endpoints
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().and()
            .authorizeRequests()
                .antMatchers("/api/auth/**", "/api/chat/webhook/n8n")
                    .permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(STATELESS)
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 8.3 Validations des Clés API
- [ ] Clé API n8n stockée en variable d'environnement
- [ ] Vérification sur chaque appel webhook
- [ ] Logging des appels n8n

---

## 🧪 PHASE 9 : Tests

### 9.1 Tests Unitaires
- [ ] AuthService Tests
- [ ] BudgetService Tests
- [ ] ExpenseService Tests
- [ ] AlertService Tests
- [ ] ChatService Tests

### 9.2 Tests Intégration
- [ ] Tests endpoints REST
- [ ] Tests JWT validation
- [ ] Tests N8n webhook
- [ ] Tests database triggers

```java
@SpringBootTest
@AutoConfigureMockMvc
public class BudgetControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateExpense() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setDescription("Test");
        
        mockMvc.perform(post("/api/expenses")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isCreated());
    }
}
```

---

## 📦 PHASE 10 : Déploiement et DevOps

### 10.1 Docker
- [ ] Dockerfile Spring Boot
- [ ] Docker Compose (Backend + PostgreSQL)
- [ ] Variables d'environnement

### 10.2 CI/CD
- [ ] Pipeline Maven (compilation, tests)
- [ ] Analyse code (SonarQube)
- [ ] Déploiement automatique

### 10.3 Documentation
- [ ] Swagger/OpenAPI
- [ ] README déploiement
- [ ] Postman Collection

---

## 📅 Timeline Estimée

| Phase | Durée | Dépendances |
|-------|-------|-------------|
| 1. Configuration | 1-2j | - |
| 2. Authentification | 2-3j | Phase 1 |
| 3. Budget & Dépenses | 3-4j | Phase 2 |
| 4. Alertes | 2j | Phase 3, DB |
| 5. Épargne | 1-2j | Phase 2 |
| 6. Intégration n8n | 3-4j | Phase 2, 3 |
| 7. Gestion erreurs | 2j | Phase 2-6 |
| 8. Sécurité | 2j | Phase 2-6 |
| 9. Tests | 4-5j | Phase 1-8 |
| 10. Déploiement | 2-3j | Phase 9 |
| **Total** | **23-28j** | |

---

## 🚀 Ordre d'Implémentation Recommandé

### Semaine 1
1. ✅ Phase 1 : Setup projet + structure
2. ✅ Phase 2 : Authentification JWT
3. ✅ Tests Auth

### Semaine 2
4. ✅ Phase 3 : Revenus et Dépenses
5. ✅ Phase 4 : Budgets et Alertes
6. ✅ Migrations Flyway (Triggers)

### Semaine 3
7. ✅ Phase 5 : Épargne
8. ✅ Phase 6 : Intégration n8n ⭐ **PRIORITÉ**
9. ✅ Tests n8n webhook

### Semaine 4
10. ✅ Phase 7 : Gestion erreurs + Phase 8 : Sécurité
11. ✅ Phase 9 : Tests complets
12. ✅ Phase 10 : Déploiement

---

## 📝 Checklist de Validation

### Avant Phase 3
- [ ] Projet Spring Boot créé
- [ ] PostgreSQL configuré
- [ ] Flyway migrations en place
- [ ] JWT fonctionnel et testé
- [ ] Authentification testée (login/register)

### Avant Phase 6
- [ ] CRUD Expense/Revenue fonctionnel
- [ ] Budgets et Alerts testés
- [ ] Base de données stable
- [ ] Endpoints sécurisés

### Avant Déploiement
- [ ] Tous les tests passent (>80% couverture)
- [ ] N8n webhook testé et fonctionnel
- [ ] Validation des entrées complète
- [ ] Documentation Swagger générée
- [ ] Docker fonctionnel

---

## 🔗 Ressources Essentielles

- [Spring Boot 3.2 Doc](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://spring.io/guides/tutorials/spring-boot-oauth2)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL Triggers](https://www.postgresql.org/docs/current/sql-createtrigger.html)
- [N8N REST API](https://docs.n8n.io/)
- [Maven Guide](https://maven.apache.org/guides/)

---

## 📌 Notes Importantes

> ⚠️ **N8N Integration** : C'est l'aspect critique. Le backend reçoit une requête POST de n8n, traite les données, et retourne une réponse. Ne pas oublier de valider la clé API n8n sur chaque requête webhook.

> 🔑 **JWT Security** : Les tokens JWT doivent être stockés en HttpOnly Cookies côté frontend, jamais dans localStorage.

> 📊 **Database Triggers** : Les triggers PostgreSQL doivent être bien testés et documentés. Prévoir des migrations Flyway séquentielles.

> 🧪 **Tests n8n** : Avant déploiement, tester le workflow n8n complet en passant par le backend.

---

**Document Version** : 1.0  
**Date** : 11 Mai 2026  
**Auteur** : Expert Backend Java  
**Statut** : 🟢 Prêt à l'implémentation
