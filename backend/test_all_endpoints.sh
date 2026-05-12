#!/bin/bash

# ============================================================
# BudgetSmart — Script de test E2E exhaustif
# Basé sur Conception/03_API_CONTRACT.md
# ============================================================

API_URL="http://localhost:8080/api"
EMAIL="test_full_$(date +%s)@budgetsmart.mg"
PASSWORD="password123!"

PASS=0; FAIL=0

# ── Utilitaires ─────────────────────────────────────────────
green() { echo -e "\e[32m✅ $1\e[0m"; }
red()   { echo -e "\e[31m❌ $1\e[0m"; }
blue()  { echo -e "\e[34m\n➡️  $1\e[0m"; }
yellow(){ echo -e "\e[33m$1\e[0m"; }

check() {
  local label=$1; local body=$2; local expected=$3
  if echo "$body" | grep -q "$expected"; then
    green "$label"
    ((PASS++))
  else
    red "$label — attendu '$expected' dans: $(echo $body | head -c 200)"
    ((FAIL++))
  fi
}

echo ""
echo "══════════════════════════════════════════════════"
echo "🚀  TEST E2E EXHAUSTIF - BUDGETSMART API"
echo "══════════════════════════════════════════════════"
echo "Utilisateur de test : $EMAIL"
echo ""

# ── 1. AUTHENTIFICATION ─────────────────────────────────────
blue "1. Authentification"

REGISTER=$(curl -s -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
check "POST /auth/register" "$REGISTER" "token"

TOKEN=$(echo $REGISTER | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
  red "Arrêt critique : Impossible de récupérer le token JWT."
  exit 1
fi
AUTH="Authorization: Bearer $TOKEN"

LOGIN=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
check "POST /auth/login" "$LOGIN" "token"

ME=$(curl -s -X GET "$API_URL/auth/me" -H "$AUTH")
check "GET /auth/me" "$ME" "$EMAIL"

UPD_PROFILE=$(curl -s -X PUT "$API_URL/auth/profile" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Nouveau\",\"monthlyBudget\":3000.00}")
check "PUT /auth/profile" "$UPD_PROFILE" "Nouveau"

UPD_PWD=$(curl -s -X PUT "$API_URL/auth/password" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"oldPassword\":\"$PASSWORD\",\"newPassword\":\"newpassword123!\"}")
check "PUT /auth/password" "$UPD_PWD" "succès"
PASSWORD="newpassword123!"

# ── 2. CATÉGORIES ────────────────────────────────────────────
blue "2. Catégories"

CAT_EXP=$(curl -s -X POST "$API_URL/categories" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"name":"Alimentation","type":"EXPENSE","color":"#ef4444"}')
check "POST /categories (Expense)" "$CAT_EXP" '"id"'
CAT_EXP_ID=$(echo $CAT_EXP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

CAT_REV=$(curl -s -X POST "$API_URL/categories" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"name":"Salaire","type":"REVENUE","color":"#22c55e"}')
check "POST /categories (Revenue)" "$CAT_REV" '"id"'
CAT_REV_ID=$(echo $CAT_REV | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

# Create a category just to delete it
CAT_DEL=$(curl -s -X POST "$API_URL/categories" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"name":"A_Supprimer","type":"EXPENSE","color":"#000000"}')
CAT_DEL_ID=$(echo $CAT_DEL | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

LIST_CAT=$(curl -s -X GET "$API_URL/categories" -H "$AUTH")
check "GET /categories" "$LIST_CAT" "Alimentation"

DEL_CAT=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$API_URL/categories/$CAT_DEL_ID" -H "$AUTH")
check "DELETE /categories/{id}" "$DEL_CAT" "204"

# ── 3. DÉPENSES ──────────────────────────────────────────────
blue "3. Dépenses"

EXP=$(curl -s -X POST "$API_URL/expenses" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":120.50,\"description\":\"Courses supermarché\",\"categoryId\":$CAT_EXP_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "POST /expenses" "$EXP" '"id"'
EXP_ID=$(echo $EXP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

GET_EXP_LIST=$(curl -s -X GET "$API_URL/expenses?page=0&size=10" -H "$AUTH")
check "GET /expenses (Paginé)" "$GET_EXP_LIST" "content"

GET_EXP_BY_ID=$(curl -s -X GET "$API_URL/expenses/$EXP_ID" -H "$AUTH")
check "GET /expenses/{id}" "$GET_EXP_BY_ID" "120.5"

UPD_EXP=$(curl -s -X PUT "$API_URL/expenses/$EXP_ID" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":150.00,\"description\":\"Courses supermarché modifiées\",\"categoryId\":$CAT_EXP_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "PUT /expenses/{id}" "$UPD_EXP" "150.0"

# Dépense à supprimer
EXP_DEL=$(curl -s -X POST "$API_URL/expenses" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":10.00,\"description\":\"A_Supprimer\",\"categoryId\":$CAT_EXP_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
EXP_DEL_ID=$(echo $EXP_DEL | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
DEL_EXP=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$API_URL/expenses/$EXP_DEL_ID" -H "$AUTH")
check "DELETE /expenses/{id}" "$DEL_EXP" "204"

SUMMARY=$(curl -s -X GET "$API_URL/expenses/summary/$(date +%Y)/$(date +%-m)" -H "$AUTH")
check "GET /expenses/summary/{year}/{month}" "$SUMMARY" "expensesByCategory"

# ── 4. REVENUS ───────────────────────────────────────────────
blue "4. Revenus"

REV=$(curl -s -X POST "$API_URL/revenues" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":3000.00,\"description\":\"Salaire\",\"categoryId\":$CAT_REV_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "POST /revenues" "$REV" '"id"'
REV_ID=$(echo $REV | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

GET_REV_LIST=$(curl -s -X GET "$API_URL/revenues" -H "$AUTH")
check "GET /revenues" "$GET_REV_LIST" "content"

GET_REV_BY_ID=$(curl -s -X GET "$API_URL/revenues/$REV_ID" -H "$AUTH")
check "GET /revenues/{id}" "$GET_REV_BY_ID" "Salaire"

UPD_REV=$(curl -s -X PUT "$API_URL/revenues/$REV_ID" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":3200.00,\"description\":\"Salaire + Prime\",\"categoryId\":$CAT_REV_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "PUT /revenues/{id}" "$UPD_REV" "3200.0"

# Revenu à supprimer
REV_DEL=$(curl -s -X POST "$API_URL/revenues" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":100.00,\"description\":\"A_Supprimer\",\"categoryId\":$CAT_REV_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
REV_DEL_ID=$(echo $REV_DEL | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
DEL_REV=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$API_URL/revenues/$REV_DEL_ID" -H "$AUTH")
check "DELETE /revenues/{id}" "$DEL_REV" "204"

# ── 5. ÉPARGNE ───────────────────────────────────────────────
blue "5. Objectifs d'épargne"

SAV=$(curl -s -X POST "$API_URL/savings" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"goalName":"Achat Voiture","targetAmount":10000.00,"targetDate":"2026-05-01"}')
check "POST /savings" "$SAV" '"id"'
SAV_ID=$(echo $SAV | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

ADD_FUNDS=$(curl -s -X POST "$API_URL/savings/$SAV_ID/add" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"amount":2000.00}')
check "POST /savings/{id}/add" "$ADD_FUNDS" "2000.0"

LIST_SAV=$(curl -s -X GET "$API_URL/savings" -H "$AUTH")
check "GET /savings" "$LIST_SAV" "Achat Voiture"

# ── 6. ALERTES ───────────────────────────────────────────────
blue "6. Alertes Budgétaires"

# Les alertes nécessitent le déclenchement d'un trigger DB, 
# la liste peut être vide pour ce nouvel utilisateur si aucun dépassement n'est atteint.
# Mais les endpoints doivent répondre en 200/204.

ALERTS_STATS=$(curl -s -X GET "$API_URL/alerts/stats" -H "$AUTH")
check "GET /alerts/stats" "$ALERTS_STATS" "total"

ALERTS_ALL=$(curl -s -X GET "$API_URL/alerts" -H "$AUTH")
check "GET /alerts" "$ALERTS_ALL" "\["

ALERTS_UNREAD=$(curl -s -X GET "$API_URL/alerts/unread" -H "$AUTH")
check "GET /alerts/unread" "$ALERTS_UNREAD" "\["

# Créons artificiellement une alerte via la DB pour tester les PUT/DELETE ? 
# On peut tester au moins le read-all qui répond 204
MARK_ALL=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$API_URL/alerts/read-all" -H "$AUTH")
check "PUT /alerts/read-all" "$MARK_ALL" "204"

# (Note: PUT /alerts/{id}/read et DELETE /alerts/{id} nécessiteraient d'avoir un ID d'alerte, 
# comme l'utilisateur vient d'être créé et n'a pas dépassé de budget, il n'y en a pas ici).

# ── 7. CHAT IA (N8N / LOCAL) ─────────────────────────────────
blue "7. Chat IA (n8n Webhook / Local)"

CHAT_STATUS=$(curl -s -X GET "$API_URL/chat/status" -H "$AUTH")
check "GET /chat/status" "$CHAT_STATUS" "n8nConnected"

CHAT_MSG=$(curl -s -X POST "$API_URL/chat/message" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"question":"Test de fonctionnement E2E"}')
check "POST /chat/message" "$CHAT_MSG" "response"

# ── 8. HEALTH CHECK ──────────────────────────────────────────
blue "8. Santé de l'API"
HEALTH=$(curl -s -X GET "$API_URL/health")
check "GET /health" "$HEALTH" "status"

# ── RÉSULTAT FINAL ──────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════"
echo "📊 RÉSULTATS : ✅ $PASS réussis | ❌ $FAIL échoués"
echo "══════════════════════════════════════════════════"

if [ $FAIL -eq 0 ]; then
  green "🎉 TOUS LES ENDPOINTS SONT OPÉRATIONNELS !"
else
  red "⚠️ Il y a $FAIL test(s) en échec."
fi
echo ""
