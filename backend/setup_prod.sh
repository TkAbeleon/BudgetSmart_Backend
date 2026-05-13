#!/bin/bash

# ============================================================
# BudgetSmart — Script d'installation Production (Alwaysdata)
# ============================================================

set -e

echo "🚀 Préparation de l'environnement de production..."

# 1. Vérification de Java
if ! command -v java &> /dev/null; then
    echo "❌ Java n'est pas installé. Veuillez l'installer (Java 17+ requis)."
    exit 1
fi

# 2. Détermination de la commande Maven
if command -v mvn &> /dev/null; then
    echo "📦 Utilisation du Maven système..."
    MVN_CMD="mvn"
elif [ -f "./mvnw" ]; then
    echo "📦 Utilisation du Maven Wrapper (système non trouvé)..."
    MVN_CMD="./mvnw"
    chmod +x mvnw
else
    echo "❌ Maven n'est pas installé et mvnw est absent."
    exit 1
fi

# 3. Compilation et packaging
echo "🏗️  Compilation du projet..."
$MVN_CMD clean package -DskipTests

echo "✅ Build terminé avec succès !"
echo "📦 Le fichier JAR est disponible dans : target/budgetsmart-1.0.0.jar"

# 4. Préparation du fichier d'exécution pour Alwaysdata
echo "📝 Création du script de démarrage start.sh..."
cat <<EOF > start.sh
#!/bin/bash
# Charger les variables d'environnement
if [ -f .env ]; then
    export \$(grep -v '^#' .env | xargs)
fi

# Lancer l'application
java -jar target/budgetsmart-1.0.0.jar
EOF

chmod +x start.sh

echo "✨ Configuration terminée."
echo "👉 Pour lancer l'application sur Alwaysdata :"
echo "   1. Copiez votre fichier .env sur le serveur"
echo "   2. Configurez un service 'User program' pointant vers start.sh"
