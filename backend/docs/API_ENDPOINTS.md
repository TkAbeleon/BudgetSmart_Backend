# 📚 Documentation des Endpoints API - BudgetSmart

## 🔍 Endpoints de Santé (Health)

### GET `/api/health`
**Description** : Endpoint de monitoring pour vérifier que l'application fonctionne correctement.

**Accès** : Public (pas d'authentification requise)

**Réponse** :
```json
{
  "application": "BudgetSmart",
  "version": "1.0.0", 
  "status": "UP",
  "timestamp": 1778551774976
}
```

**Codes de statut** :
- `200 OK` : Application fonctionne correctement
- `503 Service Unavailable` : Application en maintenance ou problème critique

### GET `/api/info`
**Description** : Endpoint d'information détaillée sur l'application.

**Accès** : Public (pas d'authentification requise)

**Réponse** :
```json
{
  "name": "BudgetSmart",
  "version": "1.0.0",
  "description": "Système de Gestion de Budget Intelligent",
  "java_version": "21.0.10",
  "os_name": "Linux"
}
```

---

## 🔌 Integration n8n

### POST `/api/chat/webhook/n8n`
**Description** : Endpoint webhook pour recevoir les requêtes de n8n et les traiter avec l'assistant IA.

**Accès** : Public (pas d'authentification requise - sécurisé par clé API n8n)

**En-têtes requis** :
- `Content-Type: application/json`
- `X-n8n-api-key: [votre-clé-api-n8n]`

**Corps de la requête** :
```json
{
  "query": "Comment puis-je économiser plus d'argent ce mois-ci ?",
  "userId": "user_123",
  "context": {
    "currentMonth": "2024-05",
    "totalExpenses": 1500.00,
    "totalRevenues": 2000.00
  }
}
```

**Réponse** :
```json
{
  "response": "Pour économiser plus ce mois-ci, je vous suggère de...",
  "suggestions": [
    "Réduire les dépenses de restauration de 20%",
    "Optimiser vos abonnements mensuels",
    "Planifier un budget hebdomadaire"
  ],
  "timestamp": "2024-05-12T02:15:00Z",
  "processingTime": 1.2
}
```

**Codes de statut** :
- `200 OK` : Traitement réussi
- `400 Bad Request` : Format de requête invalide
- `401 Unauthorized` : Clé API n8n invalide
- `500 Internal Server Error` : Erreur lors du traitement

**Configuration n8n requise** :
1. **URL du webhook** : `http://localhost:8080/api/chat/webhook/n8n`
2. **Méthode** : POST
3. **En-têtes** : `X-n8n-api-key` avec votre clé API
4. **Corps** : JSON avec la requête utilisateur

---

## 🔐 Endpoints d'Authentification

### POST `/api/auth/register`
**Description** : Inscription d'un nouvel utilisateur.

**Accès** : Public

**Corps de la requête** :
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Réponse** :
```json
{
  "message": "Inscription réussie",
  "status": "success",
  "timestamp": 1778551787269
}
```

### POST `/api/auth/login`
**Description** : Connexion d'un utilisateur existant.

**Accès** : Public

**Corps de la requête** :
```json
{
  "email": "user@example.com", 
  "password": "password123"
}
```

**Réponse** :
```json
{
  "message": "Connexion réussie",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

## 📚 Documentation Swagger

**URL** : `http://localhost:8080/api/swagger-ui.html`

La documentation Swagger interactive est disponible et contient :
- Description détaillée de tous les endpoints
- Modèles de requêtes/réponses
- Possibilité de tester les endpoints directement
- Schémas de validation

---

## 🔧 Configuration des Variables d'Environnement

Pour l'intégration n8n, configurez ces variables dans `.env` :

```bash
# Configuration n8n
N8N_WEBHOOK_URL=http://localhost:5678/webhook/chat
N8N_API_KEY=votre-clé-api-n8n-secrète
N8N_TIMEOUT=30000

# Configuration Claude API (utilisée par n8n)
ANTHROPIC_API_KEY=sk-ant-votre-clé-anthropic
```

---

## 🚀 Tests des Endpoints

### Scripts de test curl :

```bash
# Health check
curl http://localhost:8080/api/health

# Inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","firstName":"Test","lastName":"User"}'

# Test n8n webhook
curl -X POST http://localhost:8080/api/chat/webhook/n8n \
  -H "Content-Type: application/json" \
  -H "X-n8n-api-key: votre-clé-api" \
  -d '{"query":"Comment économiser de l argent?","userId":"test_user"}'
```

---

## 📊 Monitoring

### Health Check Monitoring
- Configurez votre monitoring système pour interroger `/api/health` toutes les minutes
- Alerte si le statut n'est pas "UP"
- Temps de réponse attendu : < 200ms

### Logs n8n
- Les requêtes n8n sont loggées avec le userId et timestamp
- Erreurs de traitement disponibles dans les logs applicatifs
- Métriques de temps de traitement disponibles

---

*Dernière mise à jour : 12 Mai 2026*
