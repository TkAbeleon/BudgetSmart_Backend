# 🔧 Guide Complet de Configuration Backend

## ✅ Checklist d'Initialisation

### Étape 1 : Structure du Projet
- [x] Répertoires créés
- [x] pom.xml configuré (Maven)
- [x] Classes Java de base
- [x] Configuration Spring Boot

### Étape 2 : Fichiers de Configuration
- [x] `.env.exemple` - Template des variables d'environnement
- [x] `.env` - Fichier local (à ne pas committer)
- [x] `application.properties` - Configuration par défaut
- [x] `application-dev.properties` - Profil développement
- [x] `application-prod.properties` - Profil production

### Étape 3 : Configuration Sécurité
- [x] `JwtProvider.java` - Génération et validation JWT
- [x] `JwtAuthenticationFilter.java` - Filtre JWT
- [x] `SecurityConfig.java` - Configuration Spring Security
- [x] `CorsConfig.java` - Gestion CORS

### Étape 4 : Base de Données
- [x] Migration Flyway V1 (schéma initial)
- [x] Tables et indexes
- [x] Triggers pour updated_at

### Étape 5 : Docker
- [x] `Dockerfile` - Build image Spring Boot
- [x] `docker-compose.yml` - Orchestration services
- [x] PostgreSQL + PgAdmin configurés

### Étape 6 : Documentation
- [x] `README.md` - Guide utilisateur
- [x] `SETUP.md` - Ce fichier
- [x] `.gitignore` - Exclusions git

---

## 🚀 Démarrage Rapide

### Option 1 : Script Automatisé (Recommandé)

```bash
# Rendre le script exécutable
chmod +x setup.sh

# Lancer le setup
./setup.sh dev

# Le script va :
# 1. Vérifier Java et Maven
# 2. Créer .env si absent
# 3. Générer JWT_SECRET
# 4. Télécharger les dépendances
# 5. Compiler le projet
# 6. Proposer Docker Compose
```

### Option 2 : Configuration Manuelle

```bash
# 1. Copier le template .env
cp .env.exemple .env

# 2. Éditer .env avec vos valeurs
nano .env
# Ou avec VS Code
code .env

# 3. Générer une clé JWT
openssl rand -base64 32
# Copier le résultat dans JWT_SECRET dans .env

# 4. Avec Docker Compose
docker-compose up -d

# 5. OU en local avec PostgreSQL
psql -U postgres -c "CREATE DATABASE budgetsmart;"
./mvnw spring-boot:run

# 6. Vérifier le démarrage
curl http://localhost:8080/api/health
```

---

## 📋 Configuration Variables d'Environnement (.env)

### Variables Essentielles

```env
# Profil actif
APP_PROFILE=dev

# Base de données
DB_HOST=localhost
DB_PORT=5432
DB_NAME=budgetsmart
DB_USERNAME=postgres
DB_PASSWORD=secret

# JWT (IMPORTANT: clé de 32+ caractères)
JWT_SECRET=votre-clé-secrète-ici
JWT_EXPIRATION=86400000

# N8N (à remplir si vous intégrez n8n)
N8N_WEBHOOK_URL=http://localhost:5678/webhook/chat
N8N_API_KEY=votre-clé-n8n

# Claude API (optionnel pour l'IA)
ANTHROPIC_API_KEY=votre-clé-anthropic
```

### Générer JWT_SECRET Sécurisé

```bash
# Générer une clé aléatoire Base64 de 32+ caractères
openssl rand -base64 32

# Exemple de sortie :
# aB1cD2eF3gH4iJ5kL6mN7oPqRsT8uVwXyZ9+/=

# Copier dans .env
JWT_SECRET=aB1cD2eF3gH4iJ5kL6mN7oPqRsT8uVwXyZ9+/=
```

---

## 🐳 Déploiement avec Docker

### Démarrer tous les services

```bash
# Démarrer en arrière-plan
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Voir les logs du backend uniquement
docker-compose logs -f backend

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

### Services Disponibles

| Service | URL | Credentials |
|---------|-----|-------------|
| Backend | http://localhost:8080 | - |
| PostgreSQL | localhost:5432 | postgres / secret |
| PgAdmin | http://localhost:5050 | admin@budgetsmart.local / admin |
| Swagger | http://localhost:8080/api/swagger-ui.html | - |

### Accéder à PgAdmin

1. Aller sur http://localhost:5050
2. Login : admin@budgetsmart.local / admin
3. Ajouter serveur :
   - Host: `postgres`
   - Port: `5432`
   - Username: `postgres`
   - Password: `secret`

---

## 🧪 Vérifications Après Démarrage

### 1. Health Check

```bash
curl http://localhost:8080/api/health
```

Réponse attendue :
```json
{
  "status": "UP",
  "application": "BudgetSmart",
  "version": "1.0.0",
  "timestamp": 1682345600000
}
```

### 2. Info Application

```bash
curl http://localhost:8080/api/info
```

### 3. Swagger Documentation

Accéder à : http://localhost:8080/api/swagger-ui.html

### 4. PostgreSQL Connection

```bash
# Se connecter à la DB
psql -h localhost -U postgres -d budgetsmart

# Voir les tables
\dt

# Quitter
\q
```

### 5. Vérifier les Migrations Flyway

```sql
-- Dans PostgreSQL
SELECT * FROM flyway_schema_history;
```

---

## 📝 Structure Finale du Projet

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/budgetsmart/
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── JwtProvider.java
│   │   │   ├── controller/
│   │   │   │   └── HealthController.java
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── security/
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── utils/
│   │   │   │   └── Constants.java
│   │   │   └── BudgetSmartApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/migration/
│   │           └── V1__initial_schema.sql
│   └── test/java/com/budgetsmart/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env
├── .env.exemple
├── .gitignore
├── setup.sh
├── README.md
└── SETUP.md (ce fichier)
```

---

## 🔨 Commandes Maven Utiles

```bash
# Compiler
./mvnw clean compile

# Exécuter les tests
./mvnw test

# Créer le JAR
./mvnw package

# Exécuter l'application
./mvnw spring-boot:run

# Exécuter avec profil dev
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Analyser les dépendances
./mvnw dependency:analyze

# Voir l'arborescence des dépendances
./mvnw dependency:tree

# Générer les sources (Lombok)
./mvnw clean install

# Nettoyer le projet
./mvnw clean
```

---

## ⚠️ Dépannage Courant

### Port 8080 déjà utilisé

```bash
# Changer le port dans .env
APP_PORT=8081

# Ou tuer le processus
lsof -i :8080
kill -9 <PID>
```

### PostgreSQL refuse la connexion

```bash
# Vérifier que PostgreSQL est en cours d'exécution
# Docker :
docker-compose ps

# Local :
psql -U postgres -d template1

# Vérifier .env
cat .env | grep DB_
```

### JWT_SECRET trop court

```bash
# Régénérer une clé valide (min 32 chars)
openssl rand -base64 32

# Mettre à jour .env
JWT_SECRET=nouvelle-clé
```

### Application ne démarre pas

```bash
# Voir les logs
docker-compose logs backend

# Ou en local
./mvnw spring-boot:run 2>&1 | tail -50

# Vérifier Java version
java -version

# Nettoyer et recompiler
./mvnw clean compile
```

### Migrations Flyway non exécutées

```bash
# Vérifier l'état
docker-compose exec postgres psql -U postgres -d budgetsmart -c "SELECT * FROM flyway_schema_history;"

# Réinitialiser (attention : supprime tout)
docker-compose down -v
docker-compose up -d
```

---

## 🔐 Sécurité

### Checklist Sécurité Avant Production

- [ ] JWT_SECRET modifié et sécurisé (min 256 bits)
- [ ] DB_PASSWORD fort (min 12 caractères)
- [ ] CORS_ALLOWED_ORIGINS limités aux domaines autorisés
- [ ] HTTPS activé en production
- [ ] Logs sensibles supprimés
- [ ] Profil `prod` utilisé
- [ ] N8N_API_KEY sécurisé
- [ ] Secrets stockés en variables d'environnement
- [ ] CSRF protection active
- [ ] Rate limiting configuré

---

## 📚 Ressources Utiles

- [Spring Boot 3.2 Doc](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Flyway Docs](https://flywaydb.org/)
- [JWT.io](https://jwt.io/)
- [Docker Docs](https://docs.docker.com/)

---

## ✅ Prochaines Étapes

1. **Implémenter les Entités** - User, Expense, Revenue, etc.
2. **Créer les Services** - Logique métier
3. **Développer les Controllers** - Endpoints REST
4. **Tests** - Unitaires et intégration
5. **Intégration n8n** - Webhook chat
6. **Déploiement** - Build Docker

---

**Document Version** : 1.0  
**Date** : 11 Mai 2026  
**Statut** : ✅ Prêt à l'usage
