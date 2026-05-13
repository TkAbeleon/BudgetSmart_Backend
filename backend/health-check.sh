#!/bin/bash

# ==========================================
# BudgetSmart Backend - Health Check Script
# ==========================================
# Verifies the installation and deployment
# Usage: ./health-check.sh

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  🏥 BudgetSmart Backend - Health Check"
echo "════════════════════════════════════════════════════════════════"
echo ""

ISSUES=0
WARNINGS=0

# ==========================================
# Check Java
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Java Installation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep "version" | awk -F'"' '{print $2}' | cut -d'.' -f1)
    echo "✅ Java installed: $JAVA_VERSION"
    
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "⚠️  Java 17+ required (you have $JAVA_VERSION)"
        ((ISSUES++))
    fi
else
    echo "❌ Java not found"
    ((ISSUES++))
fi

echo ""

# ==========================================
# Check Maven
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Maven Installation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version | head -1 | awk '{print $NF}')
    echo "✅ Maven installed: $MVN_VERSION"
else
    echo "❌ Maven not found"
    ((ISSUES++))
fi

echo ""

# ==========================================
# Check PostgreSQL
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "PostgreSQL Installation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v psql &> /dev/null; then
    PSQL_VERSION=$(psql --version | awk '{print $NF}')
    echo "✅ PostgreSQL client installed: $PSQL_VERSION"
else
    echo "❌ PostgreSQL client not found"
    ((ISSUES++))
fi

if command -v systemctl &> /dev/null && systemctl is-active postgresql &> /dev/null; then
    echo "✅ PostgreSQL service is running"
elif sudo -u postgres psql -c "SELECT 1" &> /dev/null 2>&1; then
    echo "✅ PostgreSQL is accessible"
else
    echo "⚠️  PostgreSQL is not running"
    ((WARNINGS++))
fi

echo ""

# ==========================================
# Check Configuration Files
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Configuration Files"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f .env ]; then
    echo "✅ .env file found"
    
    # Check required variables
    export $(grep -v '^#' .env | xargs) 2>/dev/null || true
    
    if [ -z "$DB_NAME" ]; then
        echo "⚠️  DB_NAME not set in .env"
        ((WARNINGS++))
    else
        echo "✅ DB_NAME configured: $DB_NAME"
    fi
    
    if [ -z "$JWT_SECRET" ] || grep -q "your-256-bit-secret-key-here" .env; then
        echo "⚠️  JWT_SECRET not properly configured"
        ((WARNINGS++))
    else
        echo "✅ JWT_SECRET configured"
    fi
else
    echo "❌ .env file not found"
    ((ISSUES++))
fi

if [ -f pom.xml ]; then
    echo "✅ pom.xml found"
else
    echo "❌ pom.xml not found"
    ((ISSUES++))
fi

echo ""

# ==========================================
# Check Build
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Build Status"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

JAR_FILE=$(find target -name "*.jar" -type f 2>/dev/null | head -1)
if [ -n "$JAR_FILE" ]; then
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    JAR_DATE=$(ls -lh "$JAR_FILE" | awk '{print $6, $7, $8}')
    echo "✅ JAR file built: $JAR_FILE"
    echo "   Size: $JAR_SIZE | Built: $JAR_DATE"
else
    echo "⚠️  JAR file not found (run: ./install-prod.sh)"
    ((WARNINGS++))
fi

echo ""

# ==========================================
# Check Database Connection
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Database Connection"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs) 2>/dev/null || true
    
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USERNAME" -d "$DB_NAME" -c "SELECT 1" &> /dev/null; then
        echo "✅ Database connection OK"
        echo "   Host: $DB_HOST:$DB_PORT"
        echo "   Database: $DB_NAME"
        echo "   User: $DB_USERNAME"
    else
        echo "❌ Cannot connect to database"
        echo "   Host: $DB_HOST:$DB_PORT"
        echo "   Database: $DB_NAME"
        echo "   User: $DB_USERNAME"
        ((ISSUES++))
    fi
else
    echo "⚠️  Cannot check DB connection (no .env)"
    ((WARNINGS++))
fi

echo ""

# ==========================================
# Check Service Status
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Service Status"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v systemctl &> /dev/null && systemctl is-enabled budgetsmart &> /dev/null 2>&1; then
    echo "✅ Systemd service installed"
    
    if systemctl is-active budgetsmart &> /dev/null; then
        echo "✅ Service is running"
        
        # Try to connect to the API
        if curl -s http://localhost:8080/api/health | grep -q "UP\|status" 2>/dev/null; then
            echo "✅ API is responding"
        else
            echo "⚠️  API endpoint not responding"
            ((WARNINGS++))
        fi
    else
        echo "⚠️  Service is not running"
        ((WARNINGS++))
    fi
else
    echo "⚠️  Systemd service not installed"
    ((WARNINGS++))
fi

echo ""

# ==========================================
# Check Application Server
# ==========================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Application Server"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs) 2>/dev/null || true
    
    if curl -s http://localhost:${APP_PORT:-8080}/api/health &> /dev/null; then
        echo "✅ Application is running on port ${APP_PORT:-8080}"
        
        # Get application info
        curl -s http://localhost:${APP_PORT:-8080}/api/health | head -20
    else
        echo "⚠️  Application is not running"
        echo "   Try: ./start.sh prod"
        ((WARNINGS++))
    fi
else
    echo "⚠️  Cannot check application (no .env)"
    ((WARNINGS++))
fi

echo ""

# ==========================================
# Summary
# ==========================================
echo "════════════════════════════════════════════════════════════════"

if [ $ISSUES -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "  ✅ All Systems GO!"
    echo "  🟢 The application is ready for production"
    exit 0
elif [ $ISSUES -eq 0 ]; then
    echo "  ⚠️  System Operational (with $WARNINGS warnings)"
    echo "  🟡 Some checks need attention"
    exit 0
else
    echo "  ❌ System Issues Found!"
    echo "  🔴 $ISSUES critical issue(s) / $WARNINGS warning(s)"
    exit 1
fi
