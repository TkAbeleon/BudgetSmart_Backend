# 🚀 Production Deployment Scripts - BudgetSmart Backend

Ce dossier contient tous les scripts d'installation et de gestion du backend BudgetSmart pour la production.

## 📋 Prérequis

- **Java**: 17+ (de préférence Java 21)
- **Maven**: 3.8.0+
- **PostgreSQL**: 12+ (système local, pas Docker)
- **Linux/Mac**: Ubuntu 20.04+, macOS 11+
- **Git**: Pour cloner le projet

## 🛠️ Scripts Disponibles

### 1️⃣ **install-prod.sh** - Installation Initiale (À EXÉCUTER EN PREMIER)

**Description**: Script d'installation complète qui configure tout automatiquement.

**Utilisation**:
```bash
# Installation pour production
./install-prod.sh prod

# Installation pour staging
./install-prod.sh staging

# Installation par défaut (dev)
./install-prod.sh
```

**Ce qu'il fait**:
- ✅ Vérifie Java, Maven, PostgreSQL
- ✅ Charge les variables d'environnement (.env)
- ✅ Génère un JWT secret sécurisé
- ✅ Crée la base de données PostgreSQL
- ✅ Crée l'utilisateur PostgreSQL avec permissions
- ✅ Télécharge les dépendances Maven
- ✅ Compile le projet
- ✅ Construit le JAR

**Exemple d'exécution**:
```bash
cd ~/Project/GestonBudget/backend
./install-prod.sh prod

# Sortie attendue:
# ════════════════════════════════════════════════════════════════
#   🚀 BudgetSmart Backend - Production Installation
# ════════════════════════════════════════════════════════════════
# 📋 Environment: prod
# ✅ Java 21 detected
# ✅ Maven detected: Apache Maven 3.8.7
# ✅ PostgreSQL 18.3 detected
# ✅ PostgreSQL is running
# ...
# ✅ Installation Complete!
```

---

### 2️⃣ **start.sh** - Démarrage Simple

**Description**: Démarre l'application directement avec PostgreSQL système (sans systemd).

**Utilisation**:
```bash
# Démarrer en dev
./start.sh dev

# Démarrer en prod
./start.sh prod
```

**Quand l'utiliser**:
- 🟢 Tests locaux
- 🟢 Développement
- 🟡 Petites instances (VPS personnels)

**Exemple**:
```bash
./start.sh prod

# Sortie:
# 🚀 Starting BudgetSmart Backend (prod)
# ✓ Checking PostgreSQL...
# ✅ PostgreSQL connected
# ✓ Checking application JAR...
# ✅ JAR found: target/budgetsmart-backend-1.0.0.jar
# 
# 🚀 BudgetSmart Backend
# Port: 8080
# Database: budgetsmart @ localhost:5432
# Profile: prod
# 
# . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
# 2026-05-13 08:50:00.000  INFO [...] Started BudgetSmartApplication in 3.5 seconds
```

---

### 3️⃣ **install-systemd.sh** - Installation Systemd (Production Recommandée)

**Description**: Installe l'application comme service systemd pour démarrage automatique.

**Utilisation**:
```bash
# DOIT être root
sudo ./install-systemd.sh
```

**Ce qu'il fait**:
- ✅ Crée l'utilisateur `budgetsmart`
- ✅ Copie le JAR dans `/opt/budgetsmart`
- ✅ Crée le service systemd `budgetsmart.service`
- ✅ Crée les répertoires de log
- ✅ Active le service pour démarrage automatique

**Quand l'utiliser**:
- 🔴 Production
- 🔴 Serveurs de l'entreprise
- 🔴 Instances cloud
- 🔴 Toute configuration permanente

**Exemple**:
```bash
sudo ./install-systemd.sh

# Sortie:
# 🔧 Installing BudgetSmart as Systemd Service
# ✅ Running as root
# 📝 Creating user 'budgetsmart'...
# ✅ User created
# 📁 Creating installation directory...
# 📦 Deploying JAR file...
# ✅ JAR deployed
# 📝 Copying configuration...
# ✅ Configuration deployed
# 🔧 Creating systemd service...
# ✅ Service file created
# ✅ Log directory created
# 🔄 Reloading systemd...
# 📌 Enabling service...
# 
# ════════════════════════════════════════════════════════════════
#   ✅ Systemd Service Installed!
# ════════════════════════════════════════════════════════════════
```

---

### 4️⃣ **manage.sh** - Gestion du Service

**Description**: Manage the running service (start, stop, logs, etc).

**Utilisation**:
```bash
./manage.sh [COMMAND]
```

**Commandes disponibles**:

| Commande | Description |
|----------|------------|
| `start` | Démarre le service |
| `stop` | Arrête le service |
| `restart` | Redémarre le service |
| `status` | Affiche le statut |
| `logs` | Stream les logs en temps réel |
| `log` | Affiche les 50 dernières lignes |
| `clean` | Nettoie les artifacts |
| `db-backup` | Sauvegarde la base de données |
| `db-reset` | Réinitialise la base (⚠️ destructif) |
| `help` | Affiche l'aide |

**Exemples**:
```bash
# Démarrer le service
./manage.sh start

# Voir le statut
./manage.sh status

# Stream les logs
./manage.sh logs

# Faire une sauvegarde DB
./manage.sh db-backup

# Arrêter le service
./manage.sh stop
```

---

## 📋 Workflow Complet - Installation Production

### Étape 1: Installation Initiale
```bash
cd ~/Project/GestonBudget/backend

# Exécuter l'installation
./install-prod.sh prod
```

### Étape 2: Installer comme Service Systemd
```bash
# Avec sudo
sudo ./install-systemd.sh
```

### Étape 3: Démarrer le Service
```bash
# Démarrer
./manage.sh start

# Vérifier le statut
./manage.sh status
```

### Étape 4: Vérifier que ça marche
```bash
# Stream les logs
./manage.sh logs

# Depuis un autre terminal, tester l'API
curl http://localhost:8080/api/health

# Accéder au Swagger UI
open http://localhost:8080/api/swagger-ui.html
```

---

## 🔄 Workflow Complet - Développement Local

### Étape 1: Installation
```bash
cd ~/Project/GestonBudget/backend

./install-prod.sh dev
```

### Étape 2: Démarrer pour développement
```bash
# Option A: Script simple (sans systemd)
./start.sh dev

# Option B: Maven pour live reload
./mvnw spring-boot:run
```

### Étape 3: Accéder à l'application
```bash
# Health check
curl http://localhost:8080/api/health

# Swagger UI
open http://localhost:8080/api/swagger-ui.html
```

---

## 📖 Configuration - Fichier .env

### Variables Disponibles
```env
# Spring Application
APP_PROFILE=prod
APP_PORT=8080
APP_NAME=budgetsmart

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=budgetsmart
DB_USERNAME=budgetsmart
DB_PASSWORD=votre_password_secure

# JWT Security
JWT_SECRET=votre-256-bit-secret-key-base64-encoded
JWT_EXPIRATION=86400000

# Logging
LOG_LEVEL=INFO
```

### Générer un JWT_SECRET sécurisé
```bash
# OpenSSL
openssl rand -base64 32

# Résultat exemple:
# TUkRKLFQRr9HLMHYfLLWUNI+/TZbweeu1BNZx7mjkP8=
```

---

## 🚨 Troubleshooting

### PostgreSQL n'est pas en marche
```bash
# Vérifier le statut
sudo systemctl status postgresql

# Démarrer PostgreSQL
sudo systemctl start postgresql

# Activer au démarrage
sudo systemctl enable postgresql
```

### Java non trouvé
```bash
# Installer Java 21
sudo apt-get update
sudo apt-get install openjdk-21-jdk-headless

# Vérifier
java -version
```

### Maven non trouvé
```bash
# Installer Maven
sudo apt-get install maven

# Ou utiliser le Maven wrapper
./mvnw --version
```

### Le service ne démarre pas
```bash
# Voir les logs d'erreur
./manage.sh logs

# Vérifier le statut
./manage.sh status

# Vérifier les permissions
ls -la /opt/budgetsmart/
ls -la /opt/budgetsmart/.env
```

### Erreur de connexion à la base de données
```bash
# Vérifier les credentials .env
cat .env | grep DB_

# Tester la connexion
psql -h localhost -U budgetsmart -d budgetsmart

# Vérifier les permissions PostgreSQL
sudo -u postgres psql -c "SELECT * FROM pg_roles WHERE rolname='budgetsmart';"
```

---

## 📊 Monitoring

### Voir les logs en temps réel
```bash
# Avec systemd
./manage.sh logs

# Avec le fichier log
tail -f /var/log/budgetsmart/budgetsmart.log
```

### Vérifier les endpoints
```bash
# Health check
curl -i http://localhost:8080/api/health

# Info
curl -i http://localhost:8080/api/info

# Swagger UI
open http://localhost:8080/api/swagger-ui.html
```

### Vérifier les ressources système
```bash
# Processus Java
ps aux | grep budgetsmart

# Mémoire/CPU
top

# Logs système
journalctl -u budgetsmart -f
```

---

## 🔐 Sécurité - Checklist Production

- [ ] JWT_SECRET est fort (32+ caractères aléatoires)
- [ ] DB_PASSWORD est fort et unique
- [ ] PostgreSQL n'accepte que les connexions locales (ou avec firewall)
- [ ] Spring security est activé (HTTPS en production)
- [ ] Logs n'exposent pas les données sensibles
- [ ] CORS est configuré correctement pour le domaine frontend
- [ ] Le service tourne sous un utilisateur non-root (`budgetsmart`)
- [ ] SELinux/AppArmor est configuré si applicable
- [ ] Firewall bloque les ports non-nécessaires
- [ ] Les backups automatiques sont configurés

### Activer HTTPS
```bash
# Dans application-prod.properties
server.ssl.key-store=path/to/keystore.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

---

## 📦 Sauvegarde et Restauration

### Sauvegarder la base
```bash
./manage.sh db-backup

# Ou manuellement
PGPASSWORD="$DB_PASSWORD" pg_dump -h localhost -U budgetsmart budgetsmart > backup.sql
```

### Restaurer la base
```bash
# À partir d'une sauvegarde
PGPASSWORD="$DB_PASSWORD" psql -h localhost -U budgetsmart budgetsmart < backup.sql
```

---

## 🔄 Mise à Jour du Code

### Déployer une nouvelle version
```bash
# 1. Arrêter le service
./manage.sh stop

# 2. Sauvegarder la base
./manage.sh db-backup

# 3. Compiler la nouvelle version
./mvnw clean package -DskipTests

# 4. Copier le JAR
sudo cp target/budgetsmart-backend-1.0.0.jar /opt/budgetsmart/

# 5. Redémarrer
./manage.sh restart

# 6. Vérifier
./manage.sh logs
```

---

## 📞 Support

### Endpoints Disponibles
- **Health**: `GET /api/health`
- **Info**: `GET /api/info`
- **Swagger**: `GET /api/swagger-ui.html`

### Logs Importants
- Systemd: `journalctl -u budgetsmart -f`
- Fichier: `/var/log/budgetsmart/budgetsmart.log`

### Contacts
- Backend: `/Conception/04_BACKEND.md`
- Base de Données: `/Conception/02_DATABASE.md`
- API: `/Conception/03_API_CONTRACT.md`

---

## ✅ Checklist de Vérification Post-Installation

- [ ] `./install-prod.sh prod` s'exécute sans erreurs
- [ ] PostgreSQL est connecté et la DB est créée
- [ ] Maven a compilé le projet avec succès
- [ ] `./start.sh prod` démarre l'application
- [ ] `curl http://localhost:8080/api/health` retourne 200 OK
- [ ] `sudo ./install-systemd.sh` installe le service
- [ ] `./manage.sh start` démarre le service
- [ ] `./manage.sh logs` affiche les logs sans erreurs
- [ ] Swagger UI est accessible sur http://localhost:8080/api/swagger-ui.html

---

**Dernière mise à jour**: 13 mai 2026  
**Version**: 1.0.0
