FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :microservices:gamesession-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/microservices/gamesession-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]