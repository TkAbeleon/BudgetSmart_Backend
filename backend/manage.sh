#!/bin/bash

# ==========================================
# BudgetSmart Backend - Management Script
# ==========================================
# Manage the application (start, stop, restart, status, logs)
# Usage: ./manage.sh [start|stop|restart|status|logs|clean]

set -e

COMMAND=${1:-status}
SERVICE_NAME="budgetsmart"

case $COMMAND in
    # ==========================================
    # Start Service
    # ==========================================
    start)
        echo "🚀 Starting $SERVICE_NAME service..."
        if command -v systemctl &> /dev/null; then
            sudo systemctl start "$SERVICE_NAME"
            echo "✅ Service started"
            echo ""
            systemctl status "$SERVICE_NAME"
        else
            echo "❌ Systemd not available"
            echo "   Use: ./start.sh instead"
        fi
        ;;

    # ==========================================
    # Stop Service
    # ==========================================
    stop)
        echo "⏹️  Stopping $SERVICE_NAME service..."
        if command -v systemctl &> /dev/null; then
            sudo systemctl stop "$SERVICE_NAME"
            echo "✅ Service stopped"
        else
            echo "❌ Systemd not available"
        fi
        ;;

    # ==========================================
    # Restart Service
    # ==========================================
    restart)
        echo "🔄 Restarting $SERVICE_NAME service..."
        if command -v systemctl &> /dev/null; then
            sudo systemctl restart "$SERVICE_NAME"
            echo "✅ Service restarted"
            sleep 2
            systemctl status "$SERVICE_NAME"
        else
            echo "❌ Systemd not available"
        fi
        ;;

    # ==========================================
    # Service Status
    # ==========================================
    status)
        if command -v systemctl &> /dev/null; then
            systemctl status "$SERVICE_NAME"
        else
            echo "❌ Systemd not available"
        fi
        ;;

    # ==========================================
    # View Logs
    # ==========================================
    logs)
        echo "📜 Streaming logs (Ctrl+C to exit)..."
        echo ""
        if command -v journalctl &> /dev/null; then
            sudo journalctl -u "$SERVICE_NAME" -f --output=short
        else
            tail -f /var/log/budgetsmart/budgetsmart.log 2>/dev/null || echo "Log file not found"
        fi
        ;;

    # ==========================================
    # View Recent Logs
    # ==========================================
    log)
        echo "📜 Recent logs:"
        if command -v journalctl &> /dev/null; then
            sudo journalctl -u "$SERVICE_NAME" -n 50
        else
            tail -50 /var/log/budgetsmart/budgetsmart.log 2>/dev/null || echo "Log file not found"
        fi
        ;;

    # ==========================================
    # Clean Build Artifacts
    # ==========================================
    clean)
        echo "🗑️  Cleaning build artifacts..."
        ./mvnw clean
        rm -f *.log
        echo "✅ Cleaned"
        ;;

    # ==========================================
    # Database Operations
    # ==========================================
    db-backup)
        if [ -f .env ]; then
            export $(grep -v '^#' .env | xargs)
            BACKUP_FILE="backup-${DB_NAME}-$(date +%Y%m%d-%H%M%S).sql"
            echo "💾 Backing up database to $BACKUP_FILE..."
            PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -U "$DB_USERNAME" "$DB_NAME" > "$BACKUP_FILE"
            echo "✅ Backup created: $BACKUP_FILE"
        else
            echo "❌ .env file not found"
        fi
        ;;

    db-reset)
        echo "⚠️  This will DELETE all data!"
        read -p "   Are you sure? (yes/no) " -r
        if [[ $REPLY == "yes" ]]; then
            if [ -f .env ]; then
                export $(grep -v '^#' .env | xargs)
                echo "🗑️  Dropping database..."
                sudo -u postgres psql -c "DROP DATABASE IF EXISTS \"$DB_NAME\";" 2>/dev/null || true
                echo "📝 Creating fresh database..."
                sudo -u postgres psql -c "CREATE DATABASE \"$DB_NAME\" ENCODING UTF8;"
                echo "🔐 Granting privileges..."
                sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE \"$DB_NAME\" TO \"$DB_USERNAME\";"
                echo "✅ Database reset complete"
            else
                echo "❌ .env file not found"
            fi
        else
            echo "❌ Cancelled"
        fi
        ;;

    # ==========================================
    # Help
    # ==========================================
    help|--help|-h)
        echo "════════════════════════════════════════════════════════════════"
        echo "  BudgetSmart Backend - Management Script"
        echo "════════════════════════════════════════════════════════════════"
        echo ""
        echo "Usage: ./manage.sh [COMMAND]"
        echo ""
        echo "Service Management:"
        echo "  start              Start the service"
        echo "  stop               Stop the service"
        echo "  restart            Restart the service"
        echo "  status             Show service status"
        echo ""
        echo "Logs:"
        echo "  logs               Stream logs in real-time (Ctrl+C to exit)"
        echo "  log                Show last 50 log lines"
        echo ""
        echo "Build:"
        echo "  clean              Clean build artifacts"
        echo ""
        echo "Database:"
        echo "  db-backup          Create database backup"
        echo "  db-reset           Reset database (⚠️  deletes all data)"
        echo ""
        echo "Other:"
        echo "  help               Show this help message"
        echo ""
        echo "Examples:"
        echo "  ./manage.sh start"
        echo "  ./manage.sh logs"
        echo "  ./manage.sh db-backup"
        echo ""
        echo "════════════════════════════════════════════════════════════════"
        ;;

    *)
        echo "❌ Unknown command: $COMMAND"
        echo "   Run './manage.sh help' for usage"
        exit 1
        ;;
esac
