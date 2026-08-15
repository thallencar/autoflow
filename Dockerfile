# 1. Baixa uma imagem oficial do Java (neste caso, a versão 17 da Eclipse Temurin)
FROM eclipse-temurin:25-jre

# 2. Cria e define uma pasta de trabalho dentro do container
WORKDIR /app

# 3. Copia o arquivo .jar gerado pelo Maven/Gradle para dentro do container
COPY target/autoflow-0.0.1-SNAPSHOT.jar app.jar

# 4. Define o comando padrão que será executado quando o container ligar
ENTRYPOINT ["java", "-jar", "app.jar"]