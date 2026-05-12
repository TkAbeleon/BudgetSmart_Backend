# ─── Stage 1 : Build ───────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Télécharger les dépendances (layer cache)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B 2>/dev/null || true

# Copier le code source et builder
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B -q

# ─── Stage 2 : Runtime ─────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Créer un utilisateur non-root
RUN addgroup -S budgetsmart && adduser -S budgetsmart -G budgetsmart

# Copier le JAR depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

# Changer le propriétaire
RUN chown budgetsmart:budgetsmart app.jar
USER budgetsmart

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
