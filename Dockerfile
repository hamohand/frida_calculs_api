FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copier les sources et pom.xml
COPY pom.xml .
COPY src ./src

# Installer Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Compiler le projet
RUN mvn clean package -DskipTests

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=builder /app/target/frida-calculs-api-1.0.0.jar app.jar

# Exposer le port 8081
EXPOSE 8081

# Variables d'environnement
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Healthcheck sur le nouveau endpoint avec context-path
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/calculs/actuator/health || exit 1

# Lancer l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
