#!/bin/bash

# Script de configuration PostgreSQL pour BudgetSmart
# Ce script configure la base de données et l'utilisateur

echo "🚀 Configuration PostgreSQL pour BudgetSmart..."

# Vérifier si PostgreSQL est en cours d'exécution
if ! systemctl is-active --quiet postgresql; then
    echo "❌ PostgreSQL n'est pas démarré. Démarrage..."
    sudo systemctl start postgresql
    sleep 3
fi

# Créer la base de données
echo "📦 Création de la base de données budgetsmart..."
sudo -u postgres createdb budgetsmart 2>/dev/null || echo "✅ Base de données déjà existante"

# Créer l'utilisateur
echo "👤 Création de l'utilisateur budgetsmart_user..."
sudo -u postgres psql -c "CREATE USER budgetsmart_user WITH PASSWORD 'budgetsmart_password';" 2>/dev/null || echo "✅ Utilisateur déjà existant"

# Donner les droits
echo "🔐 Configuration des droits d'accès..."
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE budgetsmart TO budgetsmart_user;"

# Importer le schéma
echo "📋 Importation du schéma de la base de données..."
psql -h localhost -U budgetsmart_user -d budgetsmart -f init.sql

echo "✅ Configuration terminée !"
echo ""
echo "📊 Informations de connexion :"
echo "   Hôte: localhost"
echo "   Port: 5432"
echo "   Base de données: budgetsmart"
echo "   Utilisateur: budgetsmart_user"
echo "   Mot de passe: budgetsmart_password"
echo ""
echo "🧪 Test de connexion :"
echo "   psql -h localhost -U budgetsmart_user -d budgetsmart -c 'SELECT COUNT(*) FROM users;'"
