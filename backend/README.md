# 🚀 BudgetSmart Backend - Guide de Démarrage

## 📋 Prérequis

- **Java** 17+
- **Maven** 3.9+
- **PostgreSQL** 15+ OU **Docker** & **Docker Compose**
- **Git**

## 🔧 Installation Rapide

### 1️⃣ Cloner et Configurer

```bash
# Copier le fichier .env d'exemple
cp .env.exemple .env

# Éditer .env avec vos configurations
nano .env
# OU
code .env
```

### 2️⃣ Options de Démarrage

#### Option A : Avec Docker Compose (Recommandé pour dev)

```bash
# Démarrer tous les services (PostgreSQL + Backend)
docker-compose up -d

# Voir les logs
docker-compose logs -f backend

# Arrêter les services
docker-compose down
```

#### Option B : En Local avec PostgreSQL

```bash
# 1. Créer la base de données PostgreSQL
createdb -U postgres budgetsmart

# 2. Démarrer le backend
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Option C : Compilation et Exécution Manuelle

```bash
# Compiler le projet
./mvnw clean package

# Exécuter le JAR
java -jar target/budgetsmart-backend-1.0.0.jar
```

---

## 📊 Configuration Environnement

### Variables .env Essentielles

```env
# Base de données
DB_HOST=localhost
DB_PORT=5432
DB_NAME=budgetsmart
DB_USERNAME=postgres
DB_PASSWORD=secret

# JWT (Générer une clé : openssl rand -base64 32)
JWT_SECRET=your-256-bit-secret-key-here

# N8N Webhook
N8N_WEBHOOK_URL=http://localhost:5678/webhook/chat
N8N_API_KEY=your-n8n-api-key

# Profil (dev ou prod)
APP_PROFILE=dev
APP_PORT=8080
```

---

## 🌐 Endpoints Disponibles

| URL | Description |
|-----|-------------|
| `http://localhost:8080/api/swagger-ui.html` | Swagger UI (Documentation API) |
| `http://localhost:8080/api/v3/api-docs` | OpenAPI JSON |
| `http://localhost:5050` | PgAdmin (Management PostgreSQL) |

### Credentials PgAdmin (Docker)
- **Email** : `admin@budgetsmart.local`
- **Password** : `admin`

---

## 📦 Structure du Projet

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/budgetsmart/
│   │   │   ├── config/          # Configuration (JWT, Security, CORS)
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── service/         # Business Logic
│   │   │   ├── repository/      # Database Access (JPA)
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Custom Exceptions
│   │   │   ├── security/        # JWT & Security
│   │   │   ├── utils/           # Utilities
│   │   │   └── BudgetSmartApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/migration/    # Flyway Migrations (SQL)
│   └── test/
├── pom.xml                       # Maven Configuration
├── Dockerfile                    # Docker Image Build
├── docker-compose.yml           # Services Orchestration
├── .env.exemple                 # Environment Variables Template
├── .env                         # Actual Environment Variables
└── README.md                    # This file
```

---

## 🧪 Tests

### Lancer les tests unitaires

```bash
# Tous les tests
./mvnw test

# Tests avec couverture
./mvnw test jacoco:report

# Tests d'intégration
./mvnw verify
```

---

## 🔨 Commandes Maven Utiles

```bash
# Nettoyer le projet
./mvnw clean

# Compiler uniquement
./mvnw compile

# Créer le JAR
./mvnw package

# Installer les dépendances
./mvnw install

# Lancer en développement
./mvnw spring-boot:run

# Lancer avec profil dev
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Générer la documentation Swagger
./mvnw springdoc-openapi:springdoc

# Vérifier les dépendances
./mvnw dependency:analyze
```

---

## 🐛 Debugging

### Logs en Temps Réel

```bash
# Avec Docker Compose
docker-compose logs -f backend --tail 100

# Avec Maven
./mvnw spring-boot:run --debug
```

### Activer Mode Debug Complet

Éditer `.env` :
```env
LOG_LEVEL=DEBUG
spring.jpa.show-sql=true
```

---

## 🔐 Sécurité

### Générer une Clé JWT Secrète

```bash
# Linux / Mac
openssl rand -base64 32

# Résultat exemple
abcdef1234567890ABCDEF1234567890==
```

Remplacer dans `.env` :
```env
JWT_SECRET=abcdef1234567890ABCDEF1234567890==
```

---

## 📝 Migration Base de Données

Les migrations Flyway s'exécutent automatiquement au démarrage :

```
db/migration/
├── V1__initial_schema.sql         # Tables initiales
├── V2__add_triggers.sql           # Triggers PostgreSQL (à venir)
└── V3__add_indexes.sql            # Index supplémentaires (à venir)
```

### Ajouter une Migration

1. Créer un fichier `V{N}__{description}.sql` dans `src/main/resources/db/migration/`
2. Les fichiers sont exécutés dans l'ordre de version
3. Redémarrer l'application

---

## ⚠️ Dépannage

### Erreur : Port 8080 déjà utilisé

```bash
# Trouver le processus utilisant le port
lsof -i :8080

# Changer le port dans .env
APP_PORT=8081
```

### Erreur : Base de données inaccessible

```bash
# Vérifier la connexion PostgreSQL
psql -h localhost -U postgres -d budgetsmart

# Vérifier les variables .env
cat .env | grep DB_
```

### Erreur : JWT Secret trop court

```bash
# Régénérer une clé valide (min 32 caractères)
openssl rand -base64 32
```

---

## 📚 Documentation Supplémentaire

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://spring.io/guides/tutorials/spring-boot-oauth2)
- [PostgreSQL Triggers](https://www.postgresql.org/docs/current/sql-createtrigger.html)
- [Flyway Migrations](https://flywaydb.org/documentation/)
- [OpenAPI / Swagger](https://springdoc.org/)

---

## 🤝 Contribution

```bash
# Créer une branche feature
git checkout -b feature/ma-feature

# Committer et push
git add .
git commit -m "feat: description"
git push origin feature/ma-feature
```

---

## 📄 License

MIT License - Voir LICENSE pour les détails

---

**Version** : 1.0.0  
**Dernière mise à jour** : 11 Mai 2026  
**Auteur** : BudgetSmart Team
