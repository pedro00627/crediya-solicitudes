# === Imagen base para construcción ===
FROM gradle:9.0.0-jdk21-jammy AS build
WORKDIR /workspace

# Copiar archivos de versiones y Common primero
COPY common-versions.gradle ./
COPY Common ./Common

# Copiar el microservicio
COPY Solicitudes ./Solicitudes

# Construir Common y luego el microservicio en un solo paso
RUN apt-get update && apt-get install -y dos2unix && \
    cd Common && chmod +x gradlew && ./gradlew publishToMavenLocal --no-daemon -x test && \
    cd ../Solicitudes && dos2unix gradlew && chmod +x gradlew && \
    ./gradlew bootJar --no-daemon -x test

# === Imagen final optimizada ===
FROM gcr.io/distroless/java21-debian12
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /workspace/Solicitudes/applications/app-service/build/libs/*.jar ./solicitudes.jar

# Exponer el puerto correcto
EXPOSE 8081

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app/solicitudes.jar"]