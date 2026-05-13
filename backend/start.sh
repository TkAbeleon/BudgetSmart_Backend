#!/bin/bash

# ============================================================
# BudgetSmart — Script de démarrage automatique (Alwaysdata)
# ============================================================

# 1. Chargement des variables d'environnement
if [ -f .env ]; then
    echo "📂 Chargement de la configuration .env..."
    # Utilisation d'une méthode robuste qui supporte les caractères spéciaux (Supabase password)
    set -a
    source .env
    set +a
else
    echo "❌ Erreur : Fichier .env manquant."
    exit 1
fi

# 2. Vérification du JAR
JAR_FILE="target/budgetsmart-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Erreur : $JAR_FILE non trouvé. Lancez ./setup_prod.sh d'abord."
    exit 1
fi

# 3. Lancement de l'application (Mode Non-Interactif)
echo "🚀 Lancement de BudgetSmart Backend..."
echo "📊 Connexion à : $DB_HOST"

# On utilise les variables d'environnement déjà chargées
# Spring Boot les détectera automatiquement
java -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -Djava.security.egd=file:/dev/./urandom \
     -jar "$JAR_FILE"
