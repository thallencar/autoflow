# Estágio 1: Compilação (Java 25 + Maven)
FROM maven:3.10.0-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Execução (JRE Java 25 válido)
FROM amazoncorretto:25-al2023-headless
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]