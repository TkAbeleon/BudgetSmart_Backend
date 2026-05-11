# 📑 Index Complet - Backend BudgetSmart

## 🎯 Fichiers de Configuration

### Configuration Application
| Fichier | Profil | Description |
|---------|--------|-------------|
| [application.properties](src/main/resources/application.properties) | - | Configuration par défaut |
| [application-dev.properties](src/main/resources/application-dev.properties) | dev | Configuration développement |
| [application-prod.properties](src/main/resources/application-prod.properties) | prod | Configuration production |

### Variables d'Environnement
| Fichier | Statut | Description |
|---------|--------|-------------|
| [.env.exemple](.env.exemple) | ✅ | Template (à committer) |
| [.env](.env) | 🚫 | Fichier local (pas de commit) |

### Build & Deploy
| Fichier | Description |
|---------|-------------|
| [pom.xml](pom.xml) | Configuration Maven |
| [Dockerfile](Dockerfile) | Build image Docker |
| [docker-compose.yml](docker-compose.yml) | Orchestration services |
| [.gitignore](.gitignore) | Exclusions git |
| [.mvn/wrapper/](mvn/wrapper) | Maven Wrapper |

---

## 📚 Documentation

| Fichier | Audience | Contenu |
|---------|----------|---------|
| [README.md](README.md) | Utilisateurs | Guide de démarrage |
| [SETUP.md](SETUP.md) | Développeurs | Configuration détaillée |
| [INITIALIZATION.md](INITIALIZATION.md) | Projet | Résumé initialisation |
| [INDEX.md](INDEX.md) | Structure | Ce fichier |

---

## 🔧 Code Source - Java

### 🎯 Classe Main
```
src/main/java/com/budgetsmart/
└── BudgetSmartApplication.java  (Point d'entrée Spring Boot)
```

### 🔐 Configuration & Sécurité
```
src/main/java/com/budgetsmart/config/
├── JwtProvider.java             (Génération/Validation JWT)
├── SecurityConfig.java          (Spring Security + JWT Filter)
└── CorsConfig.java              (Configuration CORS)

src/main/java/com/budgetsmart/security/
└── JwtAuthenticationFilter.java  (Filtre JWT pour requêtes)
```

### 🎛️ Controllers (Endpoints REST)
```
src/main/java/com/budgetsmart/controller/
├── HealthController.java        (Health check + Info)
├── AuthController.java          (À implémenter)
├── ExpenseController.java       (À implémenter)
├── RevenueController.java       (À implémenter)
├── BudgetController.java        (À implémenter)
├── SavingsController.java       (À implémenter)
├── AlertController.java         (À implémenter)
└── ChatController.java          (À implémenter - N8N)
```

### 💼 Services (Logique Métier)
```
src/main/java/com/budgetsmart/service/
├── AuthService.java             (À implémenter)
├── ExpenseService.java          (À implémenter)
├── RevenueService.java          (À implémenter)
├── BudgetService.java           (À implémenter)
├── SavingsService.java          (À implémenter)
├── AlertService.java            (À implémenter)
└── ChatService.java             (À implémenter - N8N)
```

### 📊 Repositories (Accès Base de Données)
```
src/main/java/com/budgetsmart/repository/
├── UserRepository.java          (À implémenter)
├── ExpenseRepository.java       (À implémenter)
├── RevenueRepository.java       (À implémenter)
├── BudgetRepository.java        (À implémenter)
├── SavingsRepository.java       (À implémenter)
├── AlertRepository.java         (À implémenter)
├── CategoryRepository.java      (À implémenter)
└── ChatMessageRepository.java   (À implémenter)
```

### 📦 Entités JPA (Modèles de Données)
```
src/main/java/com/budgetsmart/entity/
├── User.java                    (À implémenter)
├── Category.java                (À implémenter)
├── Revenue.java                 (À implémenter)
├── Expense.java                 (À implémenter)
├── Budget.java                  (À implémenter)
├── Savings.java                 (À implémenter)
├── Alert.java                   (À implémenter)
└── ChatMessage.java             (À implémenter)
```

### 🔄 DTOs (Transfert de Données)
```
src/main/java/com/budgetsmart/dto/
├── auth/
│   ├── LoginRequest.java        (À implémenter)
│   ├── RegisterRequest.java     (À implémenter)
│   └── AuthResponse.java        (À implémenter)
├── expense/
│   ├── ExpenseRequest.java      (À implémenter)
│   └── ExpenseResponse.java     (À implémenter)
├── budget/
│   ├── BudgetRequest.java       (À implémenter)
│   └── BudgetResponse.java      (À implémenter)
├── chat/
│   ├── ChatRequest.java         (À implémenter)
│   ├── ChatResponse.java        (À implémenter)
│   └── N8nWebhookPayload.java   (À implémenter)
└── response/
    ├── ApiResponse.java         (À implémenter)
    └── ErrorResponse.java       (À implémenter)
```

### ❌ Exceptions Custom
```
src/main/java/com/budgetsmart/exception/
├── ResourceNotFoundException.java  (Resource non trouvée)
├── UnauthorizedException.java      (Non autorisé)
├── ValidationException.java        (Validation échouée)
└── ChatException.java              (Erreur Chat/n8n)
```

### 🛠️ Utilitaires
```
src/main/java/com/budgetsmart/utils/
├── Constants.java                (Constantes globales)
├── Validators.java              (À implémenter)
└── DateUtils.java               (À implémenter)
```

---

## 📊 Base de Données

### Migrations Flyway
```
src/main/resources/db/migration/
├── V1__initial_schema.sql       (Schéma initial - Implémenter)
│   └── Tables: users, categories, revenues, expenses, budgets, savings, alerts, chat_messages
├── V2__add_triggers.sql         (À créer - Triggers budget)
├── V3__add_indexes.sql          (À créer - Index supplémentaires)
└── V4__add_default_categories.sql (À créer - Données initiales)
```

### Schéma Détaillé (V1)
```sql
-- Tables principales
users               -- Utilisateurs
categories          -- Catégories (REVENUE/EXPENSE)
revenues            -- Revenus/Entrées d'argent
expenses            -- Dépenses/Sorties d'argent
budgets             -- Budgets par catégorie
savings             -- Objectifs d'épargne
alerts              -- Alertes (dépassement budget)
chat_messages       -- Historique chat avec IA
```

---

## 🏗️ Architecture Complète

```
┌─────────────────────────────────────────┐
│         BudgetSmartApplication          │
│        Spring Boot Entry Point          │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┼──────────┐
        │          │          │
        ▼          ▼          ▼
   ┌────────┐ ┌────────┐ ┌────────┐
   │ Config │ │Security│ │ CORS   │
   └────────┘ └────────┘ └────────┘
        │
        ▼
   ┌─────────────────────┐
   │  REST Controllers   │
   │  (/api/...)         │
   └──────────┬──────────┘
              │
        ┌─────┼─────┐
        ▼     ▼     ▼
   ┌────────────────────┐
   │  Services (Logic)  │
   └──────────┬─────────┘
              │
        ┌─────┼─────┐
        ▼     ▼     ▼
   ┌────────────────────┐
   │ Repositories (JPA) │
   └──────────┬─────────┘
              │
              ▼
      ┌──────────────┐
      │ PostgreSQL   │
      └──────────────┘
```

---

## 🚀 Commandes Utiles

### Setup Initial
```bash
chmod +x setup.sh
./setup.sh dev
```

### Maven (avec wrapper)
```bash
# Compiler
./mvnw clean compile

# Tests
./mvnw test

# Build JAR
./mvnw package

# Run développement
./mvnw spring-boot:run
```

### Docker
```bash
# Démarrer
docker-compose up -d

# Logs
docker-compose logs -f backend

# Arrêter
docker-compose down
```

### Vérification
```bash
# Health
curl http://localhost:8080/api/health

# Info
curl http://localhost:8080/api/info

# Swagger
open http://localhost:8080/api/swagger-ui.html
```

---

## 📋 Statut d'Implémentation

### ✅ Complété
- [x] Structure Maven
- [x] Configuration Spring Boot
- [x] JWT Provider
- [x] Security Config + CORS
- [x] Health Controller
- [x] Exceptions Custom
- [x] Constants
- [x] Database Schema V1
- [x] Docker Setup
- [x] Documentation

### 🔄 En Cours
- [ ] Entités JPA
- [ ] Repositories
- [ ] Services
- [ ] Controllers REST
- [ ] DTOs

### ⏳ À Faire (Phase 2)
- [ ] AuthService + AuthController
- [ ] ExpenseService + ExpenseController
- [ ] RevenueService + RevenueController
- [ ] BudgetService + BudgetController
- [ ] SavingsService + SavingsController
- [ ] AlertService + AlertController
- [ ] **ChatService + ChatController (N8N)** ⭐
- [ ] Tests Unitaires
- [ ] Tests Intégration
- [ ] Deployment

---

## 🔐 Sécurité

### Endpoints Protégés vs Publics

**Publics (sans JWT):**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/chat/webhook/n8n` (avec validation X-N8N-API-KEY)
- `GET /api/health`
- `GET /api/info`
- `GET /api/swagger-ui.html`

**Protégés (JWT requis):**
- Tous les autres `/api/**`

---

## 📞 Navigation Rapide

### Configuration
- [.env.exemple](.env.exemple) - Modèle variables
- [application.properties](src/main/resources/application.properties) - Propriétés
- [SecurityConfig.java](src/main/java/com/budgetsmart/config/SecurityConfig.java) - Sécurité

### Démarrage
- [README.md](README.md) - Guide utilisateur
- [SETUP.md](SETUP.md) - Configuration détaillée
- [setup.sh](setup.sh) - Script d'initialisation

### Base de Données
- [V1__initial_schema.sql](src/main/resources/db/migration/V1__initial_schema.sql) - Schéma

### Documentation Architecture
- [Plan de Travail](../Conception/06_PLAN_TRAVAIL_BACKEND.md) - Architecture complète
- [API Contract](../Conception/03_API_CONTRACT.md) - Spécification API

---

## 💡 Points Clés à Retenir

1. **JWT Security** - Tous les endpoints sauf publics nécessitent JWT valide
2. **N8N Integration** - ChatController communique avec n8n via webhook
3. **Environment Variables** - Fichier .env critique pour configuration
4. **Flyway Migrations** - Gestion schéma via SQL versionnées
5. **Docker Ready** - Tout prêt pour conteneurisation
6. **Spring Profiles** - dev/prod pour configurations différentes

---

**Dernière mise à jour** : 11 Mai 2026  
**Statut** : ✅ Initialisation Complète  
**Prête pour** : Phase 2 (Entités & Services)
