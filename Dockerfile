FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy all project files into the container
COPY . .

# Compile all Java sources wherever they are located
RUN mkdir -p out && javac -encoding UTF-8 -d out $(find . -name "*.java")

# Ensure static web assets are copied into out/static and root static folders if present
RUN mkdir -p out/static && \
    (cp -r src/main/resources/static/* out/static/ 2>/dev/null || true) && \
    (cp -r src/main/resources/static/* static/ 2>/dev/null || true)

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "out", "StudyPortal"]
