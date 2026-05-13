#!/bin/bash

# ==========================================
# BudgetSmart Backend - Simple Start Script
# ==========================================
# Starts the application with PostgreSQL system database
# Usage: ./start.sh [dev|prod]

set -e

ENV=${1:-dev}

echo "🚀 Starting BudgetSmart Backend ($ENV)"
echo ""

# Load environment
if [ ! -f .env ]; then
    echo "❌ .env file not found"
    echo "   Run: ./install-prod.sh first"
    exit 1
fi

export $(grep -v '^#' .env | xargs)

# ==========================================
# 1. Check PostgreSQL
# ==========================================
echo "✓ Checking PostgreSQL..."

if ! psql -h "$DB_HOST" -U "$DB_USERNAME" -d "$DB_NAME" -c "SELECT 1" &> /dev/null; then
    echo "❌ Cannot connect to PostgreSQL"
    echo "   Database: $DB_NAME @ $DB_HOST:$DB_PORT"
    echo "   User: $DB_USERNAME"
    exit 1
fi

echo "✅ PostgreSQL connected"
echo ""

# ==========================================
# 2. Check JAR
# ==========================================
echo "✓ Checking application JAR..."

JAR_FILE=$(find target -name "*.jar" -type f | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR file not found"
    echo "   Run: ./mvnw package first"
    exit 1
fi

echo "✅ JAR found: $JAR_FILE"
echo ""

# ==========================================
# 3. Start application
# ==========================================
echo "════════════════════════════════════════════════════════════════"
echo "  🚀 BudgetSmart Backend"
echo "  Port: $APP_PORT"
echo "  Database: $DB_NAME @ $DB_HOST:$DB_PORT"
echo "  Profile: $ENV"
echo "════════════════════════════════════════════════════════════════"
echo ""

java -Dspring.profiles.active="$ENV" -jar "$JAR_FILE"
