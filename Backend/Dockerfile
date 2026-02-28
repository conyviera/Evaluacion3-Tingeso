# Etapa 1: build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Etapa 2: runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/proyect-Tingeso-backend.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
