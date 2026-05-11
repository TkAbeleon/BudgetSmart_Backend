# ⚡ Démarrage Rapide - BudgetSmart Backend

## 🚀 En 5 Minutes

### 1️⃣ Prérequis
```bash
java -version      # Java 17+
mvn -version       # Maven 3.9+
docker -v          # Docker (optionnel)
```

### 2️⃣ Copier le .env
```bash
cp .env.exemple .env
```

### 3️⃣ Générer JWT Secret
```bash
# Copier la sortie et la mettre dans .env
openssl rand -base64 32
```

### 4️⃣ Démarrer

**Option A : Avec Docker (Recommandé)**
```bash
docker-compose up -d
```

**Option B : Local avec PostgreSQL**
```bash
psql -U postgres -c "CREATE DATABASE budgetsmart;"
./mvnw spring-boot:run
```

### 5️⃣ Tester
```bash
# Terminal 1 - Backend
curl http://localhost:8080/api/health

# Navigateur
# http://localhost:8080/api/swagger-ui.html
```

✅ **C'est prêt !**

---

## 📂 Structure Créée

```
backend/
├── src/main/java/com/budgetsmart/     (Code Java)
├── src/main/resources/                (Config & SQL)
├── pom.xml                            (Dependencies)
├── docker-compose.yml                 (Services)
├── .env                               (Config locale)
├── .env.exemple                       (Template)
├── README.md                          (Guide complet)
├── SETUP.md                           (Configuration)
└── QUICK_START.md                     (Ce fichier)
```

---

## 🔧 Configuration Basique .env

```env
APP_PROFILE=dev
APP_PORT=8080

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=budgetsmart
DB_USERNAME=postgres
DB_PASSWORD=secret

# JWT
JWT_SECRET=VOTRE-CLÉ-GÉNÉRÉE-ICI

# N8N
N8N_WEBHOOK_URL=http://localhost:5678/webhook/chat
```

---

## 📊 Services Docker

| Service | URL | User |
|---------|-----|------|
| Backend | http://localhost:8080 | - |
| PostgreSQL | localhost:5432 | postgres |
| PgAdmin | http://localhost:5050 | admin@budgetsmart.local / admin |
| Swagger | http://localhost:8080/api/swagger-ui.html | - |

---

## 🔐 Accès Endpoints

### Sans Auth (Publics)
```bash
GET /api/health           # Health check
GET /api/info             # App info
GET /api/swagger-ui.html  # Documentation
```

### Avec Auth (À implémenter)
```bash
POST /api/auth/register   # Inscription
POST /api/auth/login      # Connexion
GET  /api/expenses        # Lister dépenses
POST /api/expenses        # Créer dépense
```

---

## ⚠️ Troubleshooting Rapide

| Problème | Solution |
|----------|----------|
| Port 8080 occupé | `APP_PORT=8081` dans .env |
| PostgreSQL refuse | `docker-compose up -d` |
| JWT invalide | `openssl rand -base64 32` |
| Migrations échouées | `docker-compose down -v && docker-compose up -d` |

---

## 📚 Voir Aussi

- [README.md](README.md) - Guide complet
- [SETUP.md](SETUP.md) - Configuration détaillée
- [INDEX.md](INDEX.md) - Index complet
- [INITIALIZATION.md](INITIALIZATION.md) - Résumé init

---

## ✨ Prochaines Étapes

1. Tester les endpoints de santé
2. Voir le plan de travail : `../Conception/06_PLAN_TRAVAIL_BACKEND.md`
3. Implémenter Phase 2 : Entités & Services

---

**5 minutes pour démarrer** ✅
