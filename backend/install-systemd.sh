#!/bin/bash

# ==========================================
# BudgetSmart Backend - Systemd Service Installation
# ==========================================
# Creates a systemd service for production deployment
# Usage: sudo ./install-systemd.sh

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_USER="budgetsmart"
APP_GROUP="budgetsmart"
INSTALL_DIR="/opt/budgetsmart"
JAR_FILE="budgetsmart-backend-1.0.0.jar"

echo "🔧 Installing BudgetSmart as Systemd Service"
echo ""

# ==========================================
# 1. Check if running as root
# ==========================================
if [ "$EUID" -ne 0 ]; then 
    echo "❌ This script must be run as root"
    echo "   Use: sudo ./install-systemd.sh"
    exit 1
fi

echo "✅ Running as root"

# ==========================================
# 2. Create application user
# ==========================================
if ! id "$APP_USER" &>/dev/null; then
    echo "📝 Creating user '$APP_USER'..."
    useradd -r -s /bin/bash "$APP_USER"
    echo "✅ User created"
else
    echo "✅ User '$APP_USER' already exists"
fi

# ==========================================
# 3. Create installation directory
# ==========================================
if [ ! -d "$INSTALL_DIR" ]; then
    echo "📁 Creating installation directory..."
    mkdir -p "$INSTALL_DIR"
fi

# ==========================================
# 4. Copy JAR file
# ==========================================
echo "📦 Deploying JAR file..."
JAR_SOURCE="$SCRIPT_DIR/target/$JAR_FILE"

if [ ! -f "$JAR_SOURCE" ]; then
    echo "❌ JAR file not found: $JAR_SOURCE"
    echo "   Run ./install-prod.sh first"
    exit 1
fi

cp "$JAR_SOURCE" "$INSTALL_DIR/"
chown "$APP_USER:$APP_GROUP" "$INSTALL_DIR/$JAR_FILE"
chmod 755 "$INSTALL_DIR/$JAR_FILE"
echo "✅ JAR deployed"

# ==========================================
# 5. Copy .env file
# ==========================================
if [ -f "$SCRIPT_DIR/.env" ]; then
    echo "📝 Copying configuration..."
    cp "$SCRIPT_DIR/.env" "$INSTALL_DIR/.env"
    chown "$APP_USER:$APP_GROUP" "$INSTALL_DIR/.env"
    chmod 600 "$INSTALL_DIR/.env"
    echo "✅ Configuration deployed"
fi

# ==========================================
# 6. Create systemd service file
# ==========================================
echo "🔧 Creating systemd service..."

cat > /etc/systemd/system/budgetsmart.service << 'EOF'
[Unit]
Description=BudgetSmart Backend - Budget Management System
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=budgetsmart
Group=budgetsmart
WorkingDirectory=/opt/budgetsmart

# Load environment variables
EnvironmentFile=/opt/budgetsmart/.env

# Java execution
ExecStart=/usr/bin/java \
    -Xmx512m \
    -Xms256m \
    -Dspring.profiles.active=prod \
    -jar /opt/budgetsmart/budgetsmart-backend-1.0.0.jar

# Restart policy
Restart=on-failure
RestartSec=10s

# Process management
StandardOutput=journal
StandardError=journal
SyslogIdentifier=budgetsmart

# Security
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=yes
ReadWritePaths=/opt/budgetsmart

# Timeouts
TimeoutStartSec=120
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
EOF

echo "✅ Service file created"

# ==========================================
# 7. Create log directory
# ==========================================
LOG_DIR="/var/log/budgetsmart"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
    chown "$APP_USER:$APP_GROUP" "$LOG_DIR"
    chmod 755 "$LOG_DIR"
    echo "✅ Log directory created: $LOG_DIR"
fi

# ==========================================
# 8. Reload systemd and enable service
# ==========================================
echo "🔄 Reloading systemd..."
systemctl daemon-reload

echo "📌 Enabling service..."
systemctl enable budgetsmart.service

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  ✅ Systemd Service Installed!"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "🚀 To start the service:"
echo "   $ sudo systemctl start budgetsmart"
echo ""
echo "📊 To check status:"
echo "   $ sudo systemctl status budgetsmart"
echo ""
echo "📜 To view logs:"
echo "   $ sudo journalctl -u budgetsmart -f"
echo "   $ tail -f $LOG_DIR/budgetsmart.log"
echo ""
echo "⏹️  To stop the service:"
echo "   $ sudo systemctl stop budgetsmart"
echo ""
echo "🔄 To restart:"
echo "   $ sudo systemctl restart budgetsmart"
echo ""
echo "════════════════════════════════════════════════════════════════"
