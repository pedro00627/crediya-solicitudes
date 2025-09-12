# === Etapa 1: Construcción (Build Stage) ===
FROM gradle:8.5.0-jdk17-jammy AS build
WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN ./gradlew build --no-daemon -x test || true

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test

# === Etapa 2: Ejecución (Runtime Stage) ===
FROM gcr.io/distroless/java17-debian11
WORKDIR /app

COPY --from=build /home/gradle/src/applications/app-service/build/libs/CrediYa.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "CrediYa.jar"]