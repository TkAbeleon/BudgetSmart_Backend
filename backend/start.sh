#!/bin/bash
# Charger les variables d'environnement
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Lancer l'application
java -jar target/budgetsmart-1.0.0.jar
