# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies separately for layer caching (only re-runs if pom.xml changes)
RUN ./mvnw dependency:go-offline --no-transfer-progress

COPY src ./src
RUN ./mvnw clean package -DskipTests --no-transfer-progress

# Stage 2: Run
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
