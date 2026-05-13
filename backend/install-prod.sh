#!/bin/bash

# ==========================================
# BudgetSmart Backend - Production Installation Script
# ==========================================
# Usage: ./install-prod.sh [prod|staging]
# This script installs dependencies and builds the application for production
# Database: Uses Supabase (cloud PostgreSQL) - configured in .env

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  🚀 BudgetSmart Backend - Production Installation"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Determine environment
ENV=${1:-prod}
echo "📋 Environment: $ENV"
echo "📊 Database: Supabase (Cloud)"
echo ""

# ==========================================
# 1. Check Prerequisites
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Step 1: Checking Prerequisites"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed"
    echo "   Install with: sudo apt-get install openjdk-21-jdk-headless"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep "version" | awk -F'"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ required (you have Java $JAVA_VERSION)"
    exit 1
fi
echo "✅ Java $JAVA_VERSION detected"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed"
    echo "   Install with: sudo apt-get install maven"
    exit 1
fi
echo "✅ Maven detected: $(mvn --version | head -1)"

echo ""

# ==========================================
# 2. Load Environment Variables
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Step 2: Loading Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ ! -f .env ]; then
    echo "⚠️  .env file not found"
    echo "   Creating from .env.exemple..."
    cp .env.exemple .env
fi

# Load .env variables
export $(grep -v '^#' .env | xargs)

echo "✅ Configuration loaded"
echo "   Database: $DB_NAME @ $DB_HOST:$DB_PORT"
echo "   User: $DB_USERNAME"
echo ""

# ==========================================
# 3. Verify Supabase Configuration
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Step 3: Verify Supabase Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check .env has Supabase credentials
if [ -z "$DB_HOST" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    echo "❌ Supabase credentials not found in .env"
    echo "   Please configure: DB_HOST, DB_USERNAME, DB_PASSWORD"
    exit 1
fi

echo "✅ Supabase configuration verified"
echo "   Host: $DB_HOST"
echo "   Port: ${DB_PORT:-6543}"
echo "   Database: ${DB_NAME:-postgres}"
echo ""

# ==========================================
# 5. Verify Database Connection
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Step 5: Verifying Database Connection"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USERNAME" -d "$DB_NAME" -c "SELECT 1" &> /dev/null; then
    echo "✅ Database connection verified"
else
    echo "❌ Failed to connect to database"
    echo "   Check your .env configuration"
    exit 1
fi

echo ""

# ==========================================
# 6. Maven Dependencies & Build
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Step 6: Maven Build"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "📦 Downloading dependencies..."
./mvnw dependency:go-offline -q 2>&1 | grep -v "WARNING" || true

echo "🔨 Compiling project..."
./mvnw clean compile -q

echo "📦 Building JAR..."
./mvnw package -DskipTests -q

JAR_FILE=$(find target -name "*.jar" -type f | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR file not found"
    exit 1
fi

echo "✅ Build successful: $JAR_FILE"
echo ""

# ==========================================
# 7. Application Configuration
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✓ Step 7: Final Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "📝 Application Configuration:"
echo "   • Profil: $APP_PROFILE"
echo "   • Port: $APP_PORT"
echo "   • Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo "   • JAR: $JAR_FILE"
echo ""

# ==========================================
# 8. Display Startup Instructions
# ==========================================
echo "════════════════════════════════════════════════════════════════"
echo "  ✅ Installation Complete!"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "🚀 To start the application:"
echo ""
echo "   Option 1: Direct with profil"
echo "   $ java -jar target/budgetsmart-backend-1.0.0.jar --spring.profiles.active=$APP_PROFILE"
echo ""
echo "   Option 2: Using Maven"
echo "   $ ./mvnw spring-boot:run -Dspring-boot.run.arguments=\"--spring.profiles.active=$APP_PROFILE\""
echo ""
echo "   Option 3: Systemd service (recommended for production)"
echo "   $ sudo ./install-systemd.sh"
echo ""
echo "📊 Endpoints:"
echo "   • Health: http://localhost:$APP_PORT/api/health"
echo "   • Info: http://localhost:$APP_PORT/api/info"
echo "   • Swagger: http://localhost:$APP_PORT/api/swagger-ui.html"
echo ""
echo "📚 Database Access:"
echo "   $ PGPASSWORD=\"$DB_PASSWORD\" psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME"
echo ""
echo "🔍 View logs:"
echo "   $ tail -f budgetsmart.log"
echo ""
echo "════════════════════════════════════════════════════════════════"
