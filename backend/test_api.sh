#!/bin/bash

# ============================================================
# BudgetSmart — Script de test E2E complet
# Phases testées : Auth, Catégories, Dépenses, Revenus,
#                  Épargne, Alertes, Chat
# ============================================================
API_URL="http://localhost:8080/api"
EMAIL="test_$(date +%s)@example.com"
PASSWORD="password123"
PASS=0; FAIL=0

# ── Utilitaires ─────────────────────────────────────────────
green() { echo -e "\e[32m✅ $1\e[0m"; }
red()   { echo -e "\e[31m❌ $1\e[0m"; }
blue()  { echo -e "\e[34m\n➡️  $1\e[0m"; }

check() {
  local label=$1; local body=$2; local expected=$3
  if echo "$body" | grep -q "$expected"; then
    green "$label"; ((PASS++))
  else
    red "$label — attendu '$expected' dans: $(echo $body | head -c 200)"
    ((FAIL++))
  fi
}

echo ""
echo "══════════════════════════════════════════════════"
echo "🚀  TEST E2E BUDGETSMART API"
echo "══════════════════════════════════════════════════"
echo "Utilisateur : $EMAIL"
echo ""

# ── 1. INSCRIPTION ───────────────────────────────────────────
blue "1. Inscription"
REGISTER=$(curl -s -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Jean\",\"lastName\":\"Dupont\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
check "Inscription réussie" "$REGISTER" "token"

TOKEN=$(echo $REGISTER | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
  red "Impossible de récupérer le token JWT — arrêt du test"
  exit 1
fi
AUTH="Authorization: Bearer $TOKEN"

# ── 2. CONNEXION ─────────────────────────────────────────────
blue "2. Connexion"
LOGIN=$(curl -s -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
check "Connexion réussie" "$LOGIN" "token"

# ── 3. PROFIL ────────────────────────────────────────────────
blue "3. Profil utilisateur"
ME=$(curl -s -X GET "$API_URL/auth/me" -H "$AUTH")
check "Profil récupéré" "$ME" "email"

# ── 4. CATÉGORIES ────────────────────────────────────────────
blue "4. Catégories"
CAT_EXP=$(curl -s -X POST "$API_URL/categories" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"name":"Alimentation","type":"EXPENSE","color":"#e74c3c"}')
check "Catégorie dépense créée" "$CAT_EXP" '"id"'
CAT_EXP_ID=$(echo $CAT_EXP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

CAT_REV=$(curl -s -X POST "$API_URL/categories" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"name":"Salaire","type":"REVENUE","color":"#2ecc71"}')
check "Catégorie revenu créée" "$CAT_REV" '"id"'
CAT_REV_ID=$(echo $CAT_REV | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

CATS=$(curl -s -X GET "$API_URL/categories" -H "$AUTH")
check "Liste catégories" "$CATS" "Alimentation"

# ── 5. DÉPENSES ──────────────────────────────────────────────
blue "5. Dépenses"
EXP=$(curl -s -X POST "$API_URL/expenses" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":75.50,\"description\":\"Courses\",\"categoryId\":$CAT_EXP_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "Dépense créée" "$EXP" '"id"'
EXP_ID=$(echo $EXP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

EXP2=$(curl -s -X POST "$API_URL/expenses" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":30.00,\"description\":\"Restaurant\",\"categoryId\":$CAT_EXP_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "2ème dépense créée" "$EXP2" '"id"'

GET_EXP=$(curl -s -X GET "$API_URL/expenses/$EXP_ID" -H "$AUTH")
check "Dépense récupérée par ID" "$GET_EXP" "Courses"

UPD_EXP=$(curl -s -X PUT "$API_URL/expenses/$EXP_ID" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"amount":80.00,"description":"Courses (modifié)"}')
check "Dépense modifiée" "$UPD_EXP" "modifi"

SUMMARY=$(curl -s -X GET "$API_URL/expenses/summary/$(date +%Y)/$(date +%-m)" -H "$AUTH")
check "Résumé mensuel dépenses" "$SUMMARY" "totalExpenses"

# ── 6. REVENUS ───────────────────────────────────────────────
blue "6. Revenus"
REV=$(curl -s -X POST "$API_URL/revenues" -H "$AUTH" -H "Content-Type: application/json" \
  -d "{\"amount\":2500.00,\"description\":\"Salaire Mai\",\"categoryId\":$CAT_REV_ID,\"date\":\"$(date +%Y-%m-%d)\"}")
check "Revenu créé" "$REV" '"id"'
REV_ID=$(echo $REV | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

GET_REV=$(curl -s -X GET "$API_URL/revenues/$REV_ID" -H "$AUTH")
check "Revenu récupéré par ID" "$GET_REV" "Salaire"

LIST_REV=$(curl -s -X GET "$API_URL/revenues" -H "$AUTH")
check "Liste revenus" "$LIST_REV" "content"

# ── 7. ÉPARGNE ───────────────────────────────────────────────
blue "7. Objectifs d'épargne"
SAV=$(curl -s -X POST "$API_URL/savings" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"goalName":"Vacances","targetAmount":1500.00,"targetDate":"2026-12-31"}')
check "Objectif créé" "$SAV" "goalName"
SAV_ID=$(echo $SAV | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

ADD=$(curl -s -X POST "$API_URL/savings/$SAV_ID/add" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"amount":300.00}')
check "Montant ajouté à l'objectif" "$ADD" "currentAmount"
check "Progression calculée" "$ADD" "progressPercent"

LIST_SAV=$(curl -s -X GET "$API_URL/savings" -H "$AUTH")
check "Liste objectifs" "$LIST_SAV" "Vacances"

# ── 8. ALERTES ───────────────────────────────────────────────
blue "8. Alertes budgétaires"
ALERT_STATS=$(curl -s -X GET "$API_URL/alerts/stats" -H "$AUTH")
check "Stats alertes" "$ALERT_STATS" "total"

ALERTS=$(curl -s -X GET "$API_URL/alerts" -H "$AUTH")
check "Liste alertes" "$ALERTS" "\[" # tableau JSON

UNREAD=$(curl -s -X GET "$API_URL/alerts/unread" -H "$AUTH")
check "Alertes non lues" "$UNREAD" "\["

# Marquer tout comme lu (même si vide)
MARK=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$API_URL/alerts/read-all" -H "$AUTH")
check "Marquer tout comme lu (204)" "$MARK" "204"

# ── 9. CHAT (fallback local si n8n non configuré) ────────────
blue "9. Chat IA (fallback local)"
CHAT=$(curl -s -X POST "$API_URL/chat/message" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"question":"Combien ai-je dépensé ce mois ?"}')
check "Chat dépenses" "$CHAT" "response"

CHAT2=$(curl -s -X POST "$API_URL/chat/message" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"question":"Mes alertes budgétaires"}')
check "Chat alertes" "$CHAT2" "response"

CHAT3=$(curl -s -X POST "$API_URL/chat/message" -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"question":"Conseils épargne"}')
check "Chat épargne" "$CHAT3" "suggestions"

CHAT_STATUS=$(curl -s -X GET "$API_URL/chat/status" -H "$AUTH")
check "Statut chat service" "$CHAT_STATUS" "version"

# ── 10. HEALTH CHECK ────────────────────────────────────────
blue "10. Health check"
HEALTH=$(curl -s -X GET "$API_URL/health")
check "Health check" "$HEALTH" "status"

# ── RÉSULTAT FINAL ──────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════"
echo "📊 RÉSULTATS : ✅ $PASS réussis | ❌ $FAIL échoués"
echo "══════════════════════════════════════════════════"

[ $FAIL -eq 0 ] && echo -e "\e[32m🎉 TOUS LES TESTS PASSENT !\e[0m" \
                || echo -e "\e[31m⚠️  $FAIL test(s) en échec\e[0m"
echo ""
