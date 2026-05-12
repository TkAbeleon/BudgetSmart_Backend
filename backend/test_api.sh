#!/bin/bash

# Configuration
API_URL="http://localhost:8080/api"
# On génère un email unique pour pouvoir relancer le script plusieurs fois
EMAIL="test_$(date +%s)@example.com"
PASSWORD="password123"

echo "=========================================="
echo "🚀 DÉBUT DU TEST DE L'API BUDGETSMART"
echo "=========================================="
echo "Utilisateur : $EMAIL"
echo ""

# 1. Inscription
echo "➡️ 1. Inscription d'un nouvel utilisateur..."
REGISTER_RESP=$(curl -s -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "'$EMAIL'",
    "password": "'$PASSWORD'"
  }')

echo "Réponse: $REGISTER_RESP"
echo ""

# 2. Connexion pour récupérer le token
echo "➡️ 2. Connexion..."
LOGIN_RESP=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL'",
    "password": "'$PASSWORD'"
  }')

TOKEN=$(echo $LOGIN_RESP | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Erreur: Impossible de récupérer le token. Vérifiez si l'API est bien lancée."
    exit 1
fi

echo "✅ Token JWT récupéré avec succès !"
echo ""

# 3. Création d'une Catégorie Dépense
echo "➡️ 3. Création d'une catégorie de dépense..."
CAT_EXP_RESP=$(curl -s -X POST "$API_URL/categories" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alimentation",
    "type": "EXPENSE",
    "color": "#ff0000"
  }')
echo "Réponse: $CAT_EXP_RESP"
CAT_EXP_ID=$(echo $CAT_EXP_RESP | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo ""

# 4. Création d'une Catégorie Revenu
echo "➡️ 4. Création d'une catégorie de revenu..."
CAT_REV_RESP=$(curl -s -X POST "$API_URL/categories" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Salaire",
    "type": "REVENUE",
    "color": "#00ff00"
  }')
echo "Réponse: $CAT_REV_RESP"
CAT_REV_ID=$(echo $CAT_REV_RESP | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo ""

# 5. Ajout d'une Dépense
echo "➡️ 5. Ajout d'une dépense..."
curl -s -X POST "$API_URL/expenses" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.25,
    "description": "Courses supermarché",
    "categoryId": '$CAT_EXP_ID',
    "date": "2026-05-12"
  }' | jq 2>/dev/null || echo "Dépense ajoutée"
echo ""

# 6. Ajout d'un Revenu
echo "➡️ 6. Ajout d'un revenu..."
curl -s -X POST "$API_URL/revenues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2000.00,
    "description": "Salaire de Mai",
    "categoryId": '$CAT_REV_ID',
    "date": "2026-05-12"
  }' | jq 2>/dev/null || echo "Revenu ajouté"
echo ""

# 7. Création d'un Objectif d'épargne
echo "➡️ 7. Ajout d'un objectif d'épargne..."
curl -s -X POST "$API_URL/savings" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "goalName": "Voyage",
    "targetAmount": 1000.00,
    "targetDate": "2026-12-31"
  }' | jq 2>/dev/null || echo "Objectif ajouté"
echo ""

# 8. Récupération du résumé mensuel
echo "➡️ 8. Résumé du mois (Mai 2026)..."
curl -s -X GET "$API_URL/expenses/summary/2026/5" \
  -H "Authorization: Bearer $TOKEN" | jq 2>/dev/null || curl -s -X GET "$API_URL/expenses/summary/2026/5" -H "Authorization: Bearer $TOKEN"
echo ""
echo ""
echo "=========================================="
echo "🎉 TEST TERMINÉ"
echo "=========================================="
