# Estágio 1: Compilação (precisa de uma imagem com JDK 25 e Maven)
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Execução (apenas o JRE para rodar o .jar)
FROM eclipse-temurin:25-jre
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/target/autoflow-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]