# 📡 Contrat API REST — BudgetSmart

## Conventions générales

- **Base URL** : `http://localhost:8080/api`
- **Format** : JSON
- **Auth** : Header `Authorization: Bearer <JWT_TOKEN>` (sauf auth endpoints)
- **Dates** : ISO 8601 — `YYYY-MM-DD`
- **Montants** : Décimal avec 2 décimales — `1250.50`

---

## Format des réponses

### Succès
```json
{
  "success": true,
  "data": { ... },
  "message": "Opération réussie"
}
```

### Erreur
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Description lisible",
    "details": ["Le champ email est invalide"]
  }
}
```

### Codes d'erreur applicatifs

| Code | Signification |
|------|--------------|
| `VALIDATION_ERROR` | Champs invalides ou manquants |
| `AUTH_INVALID_CREDENTIALS` | Email ou mot de passe incorrect |
| `AUTH_TOKEN_EXPIRED` | Token JWT expiré |
| `AUTH_UNAUTHORIZED` | Ressource protégée, token manquant |
| `EMAIL_ALREADY_EXISTS` | Email déjà utilisé |
| `NOT_FOUND` | Ressource introuvable |
| `FORBIDDEN` | L'utilisateur n'a pas accès à cette ressource |

---

## 1. Authentification

### `POST /api/auth/register` — Inscription

**Body**
```json
{
  "name": "Alice Dupont",
  "email": "alice@example.com",
  "password": "MonMotDePasse123!"
}
```

**Réponse 201 Created**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsImlhdCI6MTcxNTAwMDAwMCwiZXhwIjoxNzE1MDg2NDAwfQ.abc123",
    "user": {
      "id": 1,
      "name": "Alice Dupont",
      "email": "alice@example.com",
      "monthlyBudget": 0,
      "createdAt": "2025-05-11"
    }
  },
  "message": "Compte créé avec succès"
}
```

**Erreur 400 — Email déjà existant**
```json
{
  "success": false,
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "Cette adresse email est déjà utilisée",
    "details": []
  }
}
```

**Erreur 400 — Validation**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Données invalides",
    "details": [
      "Le mot de passe doit contenir au moins 8 caractères",
      "L'email est invalide"
    ]
  }
}
```

---

### `POST /api/auth/login` — Connexion

**Body**
```json
{
  "email": "alice@example.com",
  "password": "MonMotDePasse123!"
}
```

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "Alice Dupont",
      "email": "alice@example.com",
      "monthlyBudget": 2000.00
    }
  },
  "message": "Connexion réussie"
}
```

**Erreur 401 — Identifiants incorrects**
```json
{
  "success": false,
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "Email ou mot de passe incorrect",
    "details": []
  }
}
```

---

## 2. Utilisateur

### `GET /api/users/me` — Profil courant

**Headers** : `Authorization: Bearer <token>`

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Alice Dupont",
    "email": "alice@example.com",
    "monthlyBudget": 2000.00,
    "createdAt": "2025-01-15"
  }
}
```

---

### `PUT /api/users/me` — Modifier le profil / budget

**Body**
```json
{
  "name": "Alice Martin",
  "monthlyBudget": 2500.00
}
```

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Alice Martin",
    "email": "alice@example.com",
    "monthlyBudget": 2500.00
  },
  "message": "Profil mis à jour"
}
```

---

## 3. Catégories

### `GET /api/categories` — Liste des catégories

**Query params** : `?type=EXPENSE` ou `?type=REVENUE` (optionnel)

**Réponse 200 OK**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Alimentation", "type": "EXPENSE", "color": "#ef4444" },
    { "id": 2, "name": "Transport",    "type": "EXPENSE", "color": "#f97316" },
    { "id": 7, "name": "Salaire",      "type": "REVENUE", "color": "#22c55e" }
  ]
}
```

---

### `POST /api/categories` — Créer une catégorie

**Body**
```json
{
  "name": "Abonnements",
  "type": "EXPENSE",
  "color": "#a855f7"
}
```

**Réponse 201 Created**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "name": "Abonnements",
    "type": "EXPENSE",
    "color": "#a855f7"
  },
  "message": "Catégorie créée"
}
```

---

### `DELETE /api/categories/{id}` — Supprimer

**Réponse 200 OK**
```json
{
  "success": true,
  "message": "Catégorie supprimée"
}
```

**Erreur 404**
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Catégorie introuvable",
    "details": []
  }
}
```

---

## 4. Revenus

### `GET /api/revenues` — Liste des revenus

**Query params** :
- `?month=2025-05` — filtre par mois (optionnel)
- `?page=0&size=20` — pagination

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "amount": 2000.00,
        "description": "Salaire mai",
        "date": "2025-05-01",
        "category": { "id": 7, "name": "Salaire", "color": "#22c55e" }
      },
      {
        "id": 2,
        "amount": 350.00,
        "description": "Mission freelance",
        "date": "2025-05-10",
        "category": { "id": 8, "name": "Freelance", "color": "#10b981" }
      }
    ],
    "total": 2350.00,
    "count": 2
  }
}
```

---

### `POST /api/revenues` — Ajouter un revenu

**Body**
```json
{
  "amount": 2000.00,
  "description": "Salaire mai",
  "date": "2025-05-01",
  "categoryId": 7
}
```

**Réponse 201 Created**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "amount": 2000.00,
    "description": "Salaire mai",
    "date": "2025-05-01",
    "category": { "id": 7, "name": "Salaire", "color": "#22c55e" }
  },
  "message": "Revenu ajouté"
}
```

---

### `PUT /api/revenues/{id}` — Modifier un revenu

**Body** *(champs partiels acceptés)*
```json
{
  "amount": 2100.00,
  "description": "Salaire mai (révisé)"
}
```

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "amount": 2100.00,
    "description": "Salaire mai (révisé)",
    "date": "2025-05-01",
    "category": { "id": 7, "name": "Salaire", "color": "#22c55e" }
  },
  "message": "Revenu modifié"
}
```

---

### `DELETE /api/revenues/{id}` — Supprimer

**Réponse 200 OK**
```json
{
  "success": true,
  "message": "Revenu supprimé"
}
```

**Erreur 403 — Ressource d'un autre utilisateur**
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "Accès refusé à cette ressource",
    "details": []
  }
}
```

---

## 5. Dépenses

### `GET /api/expenses` — Liste des dépenses

**Query params** :
- `?month=2025-05`
- `?categoryId=1`
- `?page=0&size=20`

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "amount": 120.50,
        "description": "Courses Carrefour",
        "date": "2025-05-03",
        "category": { "id": 1, "name": "Alimentation", "color": "#ef4444" }
      },
      {
        "id": 2,
        "amount": 65.00,
        "description": "Abonnement transport",
        "date": "2025-05-05",
        "category": { "id": 2, "name": "Transport", "color": "#f97316" }
      }
    ],
    "total": 185.50,
    "count": 2
  }
}
```

---

### `POST /api/expenses` — Ajouter une dépense

**Body**
```json
{
  "amount": 120.50,
  "description": "Courses Carrefour",
  "date": "2025-05-03",
  "categoryId": 1
}
```

**Réponse 201 Created**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "amount": 120.50,
    "description": "Courses Carrefour",
    "date": "2025-05-03",
    "category": { "id": 1, "name": "Alimentation", "color": "#ef4444" }
  },
  "message": "Dépense ajoutée"
}
```

> **Note** : L'ajout d'une dépense déclenche automatiquement le trigger PostgreSQL qui peut créer une alerte.

---

### `PUT /api/expenses/{id}` — Modifier

**Body**
```json
{
  "amount": 95.00,
  "description": "Courses Monoprix"
}
```

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "amount": 95.00,
    "description": "Courses Monoprix",
    "date": "2025-05-03",
    "category": { "id": 1, "name": "Alimentation", "color": "#ef4444" }
  },
  "message": "Dépense modifiée"
}
```

---

### `DELETE /api/expenses/{id}` — Supprimer

**Réponse 200 OK**
```json
{
  "success": true,
  "message": "Dépense supprimée"
}
```

---

## 6. Épargne

### `GET /api/savings` — Liste des objectifs d'épargne

**Réponse 200 OK**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Voyage Japon",
      "targetAmount": 3000.00,
      "currentAmount": 850.00,
      "progressPercent": 28.3,
      "deadline": "2025-12-01",
      "remaining": 2150.00
    },
    {
      "id": 2,
      "name": "Fonds urgence",
      "targetAmount": 5000.00,
      "currentAmount": 5000.00,
      "progressPercent": 100.0,
      "deadline": null,
      "remaining": 0.00
    }
  ]
}
```

---

### `POST /api/savings` — Créer un objectif

**Body**
```json
{
  "name": "Voyage Japon",
  "targetAmount": 3000.00,
  "deadline": "2025-12-01"
}
```

**Réponse 201 Created**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Voyage Japon",
    "targetAmount": 3000.00,
    "currentAmount": 0.00,
    "progressPercent": 0.0,
    "deadline": "2025-12-01",
    "remaining": 3000.00
  },
  "message": "Objectif d'épargne créé"
}
```

---

### `PATCH /api/savings/{id}/deposit` — Ajouter un versement

**Body**
```json
{
  "amount": 200.00
}
```

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Voyage Japon",
    "targetAmount": 3000.00,
    "currentAmount": 1050.00,
    "progressPercent": 35.0,
    "remaining": 1950.00
  },
  "message": "Versement enregistré"
}
```

---

### `DELETE /api/savings/{id}` — Supprimer un objectif

**Réponse 200 OK**
```json
{
  "success": true,
  "message": "Objectif supprimé"
}
```

---

## 7. Alertes

### `GET /api/alerts` — Alertes non lues

**Réponse 200 OK**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "level": "WARNING",
      "message": "⚠️ Vous avez consommé 82% de votre budget mensuel.",
      "isRead": false,
      "createdAt": "2025-05-10T14:32:00"
    },
    {
      "id": 4,
      "level": "CRITICAL",
      "message": "🔴 Attention ! 91% de votre budget est utilisé.",
      "isRead": false,
      "createdAt": "2025-05-11T09:15:00"
    }
  ],
  "unreadCount": 2
}
```

---

### `PATCH /api/alerts/{id}/read` — Marquer comme lue

**Réponse 200 OK**
```json
{
  "success": true,
  "message": "Alerte marquée comme lue"
}
```

---

### `PATCH /api/alerts/read-all` — Tout marquer comme lu

**Réponse 200 OK**
```json
{
  "success": true,
  "message": "Toutes les alertes marquées comme lues"
}
```

---

## 8. Dashboard

### `GET /api/dashboard` — Résumé du mois courant

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "month": "2025-05",
    "totalRevenues": 2350.00,
    "totalExpenses": 1840.00,
    "balance": 510.00,
    "monthlyBudget": 2000.00,
    "budgetUsedPercent": 92.0,
    "totalSavings": 1050.00,
    "unreadAlerts": 2,
    "expensesByCategory": [
      { "categoryName": "Alimentation", "color": "#ef4444", "total": 620.00, "percent": 33.7 },
      { "categoryName": "Logement",     "color": "#eab308", "total": 800.00, "percent": 43.5 },
      { "categoryName": "Transport",    "color": "#f97316", "total": 130.00, "percent": 7.1  },
      { "categoryName": "Loisirs",      "color": "#8b5cf6", "total": 290.00, "percent": 15.8 }
    ]
  }
}
```

---

## 9. Chat IA

### `POST /api/chat` — Envoyer un message à l'assistant

**Body**
```json
{
  "message": "Combien ai-je dépensé en alimentation ce mois-ci ?",
  "conversationHistory": [
    {
      "role": "user",
      "content": "Bonjour"
    },
    {
      "role": "assistant",
      "content": "Bonjour ! Je suis votre assistant budget. Comment puis-je vous aider ?"
    }
  ]
}
```

**Réponse 200 OK**
```json
{
  "success": true,
  "data": {
    "reply": "En mai 2025, vous avez dépensé **620 €** en alimentation, soit 33,7% de vos dépenses totales. C'est légèrement au-dessus de la moyenne des 3 derniers mois (580 €). Je vous suggère de vérifier vos achats en grande surface.",
    "role": "assistant"
  }
}
```

**Erreur 503 — IA indisponible**
```json
{
  "success": false,
  "error": {
    "code": "AI_SERVICE_UNAVAILABLE",
    "message": "Le service IA est temporairement indisponible",
    "details": []
  }
}
```

---

## Récapitulatif des endpoints

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Inscription |
| POST | `/api/auth/login` | ❌ | Connexion |
| GET | `/api/users/me` | ✅ | Mon profil |
| PUT | `/api/users/me` | ✅ | Modifier profil |
| GET | `/api/categories` | ✅ | Liste catégories |
| POST | `/api/categories` | ✅ | Créer catégorie |
| DELETE | `/api/categories/{id}` | ✅ | Supprimer catégorie |
| GET | `/api/revenues` | ✅ | Liste revenus |
| POST | `/api/revenues` | ✅ | Ajouter revenu |
| PUT | `/api/revenues/{id}` | ✅ | Modifier revenu |
| DELETE | `/api/revenues/{id}` | ✅ | Supprimer revenu |
| GET | `/api/expenses` | ✅ | Liste dépenses |
| POST | `/api/expenses` | ✅ | Ajouter dépense |
| PUT | `/api/expenses/{id}` | ✅ | Modifier dépense |
| DELETE | `/api/expenses/{id}` | ✅ | Supprimer dépense |
| GET | `/api/savings` | ✅ | Objectifs épargne |
| POST | `/api/savings` | ✅ | Créer objectif |
| PATCH | `/api/savings/{id}/deposit` | ✅ | Verser dans objectif |
| DELETE | `/api/savings/{id}` | ✅ | Supprimer objectif |
| GET | `/api/alerts` | ✅ | Alertes non lues |
| PATCH | `/api/alerts/{id}/read` | ✅ | Marquer lue |
| PATCH | `/api/alerts/read-all` | ✅ | Tout marquer lu |
| GET | `/api/dashboard` | ✅ | Résumé mensuel |
| POST | `/api/chat` | ✅ | Message IA |
