# Estágio 1: Build da aplicação usando o Maven Wrapper
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia os arquivos necessários para o build
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Dá permissão de execução para o Maven Wrapper e faz o build
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Estágio 2: Imagem final apenas com o JRE (mais leve para produção)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o arquivo .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que a aplicação Spring Boot usa
EXPOSE 8089

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]