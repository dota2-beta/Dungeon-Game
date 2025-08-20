FROM gradle:8.10-jdk17 as builder
ENV GRADLE_USER_HOME=/opt/gradle/caches
WORKDIR /opt/app
COPY build.gradle settings.gradle ./
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN ./gradlew dependencies
COPY src ./src
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]