#!/bin/bash

# ==========================================
# BudgetSmart Backend - Setup Script
# ==========================================
# Usage: ./setup.sh [dev|prod]

set -e

echo "🚀 BudgetSmart Backend - Initialisation..."

# Determine profile
PROFILE=${1:-dev}

echo "📋 Profil : $PROFILE"

# ==========================================
# 1. Check Prerequisites
# ==========================================
echo ""
echo "🔍 Vérification des prérequis..."

if ! command -v java &> /dev/null; then
    echo "❌ Java n'est pas installé"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven n'est pas installé"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep "version" | awk -F'"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ requis (vous avez Java $JAVA_VERSION)"
    exit 1
fi

echo "✅ Java $JAVA_VERSION détecté"

# ==========================================
# 2. Setup Environment
# ==========================================
echo ""
echo "⚙️  Configuration de l'environnement..."

if [ ! -f .env ]; then
    echo "📝 Création du fichier .env..."
    cp .env.exemple .env
    echo "⚠️  Veuillez éditer .env avec vos paramètres"
    echo "   Notamment: JWT_SECRET, N8N_API_KEY, ANTHROPIC_API_KEY"
fi

# ==========================================
# 3. Generate JWT Secret if needed
# ==========================================
if grep -q "your-256-bit-secret-key-here" .env; then
    echo ""
    echo "🔐 Génération d'une clé JWT secrète..."
    JWT_SECRET=$(openssl rand -base64 32)
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/your-256-bit-secret-key-here-minimum-32-characters/$JWT_SECRET/" .env
    else
        # Linux
        sed -i "s/your-256-bit-secret-key-here-minimum-32-characters/$JWT_SECRET/" .env
    fi
    
    echo "✅ JWT_SECRET généré et mis à jour"
fi

# ==========================================
# 4. Download Dependencies
# ==========================================
echo ""
echo "📦 Téléchargement des dépendances Maven..."
./mvnw dependency:go-offline -q

echo "✅ Dépendances téléchargées"

# ==========================================
# 5. Build Project
# ==========================================
echo ""
echo "🔨 Compilation du projet..."
./mvnw clean compile -q

echo "✅ Compilation réussie"

# ==========================================
# 6. Docker Setup (if available)
# ==========================================
if command -v docker &> /dev/null; then
    echo ""
    echo "🐳 Docker détecté"
    
    read -p "Voulez-vous utiliser Docker Compose ? (o/n) " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        echo "🚀 Démarrage avec Docker Compose..."
        docker-compose up -d
        
        echo ""
        echo "✅ Services démarrés :"
        echo "   - PostgreSQL : localhost:5432"
        echo "   - Backend : localhost:8080"
        echo "   - PgAdmin : localhost:5050"
        echo ""
        echo "⏳ Attente du démarrage du backend..."
        sleep 10
        
        # Check health
        if curl -s http://localhost:8080/api/health > /dev/null; then
            echo "✅ Backend est en ligne"
        else
            echo "⚠️  Backend en cours de démarrage, veuillez attendre..."
        fi
    fi
else
    echo "⚠️  Docker n'est pas détecté, vous devez configurer PostgreSQL manuellement"
fi

# ==========================================
# 7. Display Summary
# ==========================================
echo ""
echo "=========================================="
echo "✅ Initialisation Terminée !"
echo "=========================================="
echo ""
echo "📚 Prochaines étapes :"
echo ""
echo "1. Éditer le fichier .env si nécessaire :"
echo "   cat .env"
echo ""
echo "2. Démarrer le backend :"
echo "   ./mvnw spring-boot:run"
echo ""
echo "3. Accéder à l'API :"
echo "   - Swagger UI : http://localhost:8080/api/swagger-ui.html"
echo "   - Health Check : http://localhost:8080/api/health"
echo "   - API Docs : http://localhost:8080/api/v3/api-docs"
echo ""
echo "4. Pour Docker Compose :"
echo "   docker-compose up -d"
echo "   docker-compose logs -f backend"
echo ""
echo "=========================================="
