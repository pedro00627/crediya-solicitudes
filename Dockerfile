# === Etapa 1: Construcción (Build Stage) ===
FROM gradle:9.0.0-jdk21-jammy
WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test

FROM gcr.io/distroless/java21-debian12
WORKDIR /app

COPY --from=build /home/gradle/src/applications/app-service/build/libs/CrediYa.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
