# --- Stage 1: Build Java Bytecode ---
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY src/ ./src/
RUN mkdir -p out && javac -encoding UTF-8 -d out src/main/java/*.java

# --- Stage 2: Ultra-Lightweight Production Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled classes and static web assets
COPY --from=builder /app/out ./out
COPY src/main/resources/static ./src/main/resources/static

# Dynamic port assignment for cloud platforms (Render, Koyeb, Railway)
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "out", "StudyPortal"]

