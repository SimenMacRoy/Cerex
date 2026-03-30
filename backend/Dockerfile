# ─────────────────────────────────────────────────────────
# Multi-stage Dockerfile for Cerex Backend
# ─────────────────────────────────────────────────────────

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for dependency caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build without tests (tests run in CI)
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────────────────────
# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Security: run as non-root
RUN addgroup -g 1001 -S cerex && \
    adduser -u 1001 -S cerex -G cerex

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Switch to non-root user
USER cerex

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
