# Estágio 1: Construção do JAR (Build)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# 1. Copia apenas o manifesto de dependências primeiro
COPY pom.xml .

# 2. Baixa as dependências e plugins (Cacheado se o pom.xml não mudar)
RUN mvn dependency:go-offline

# 3. Copia o código fonte (Isso muda com frequência)
COPY src ./src

# 4. Compila usando as dependências já baixadas (Offline mode opcional aqui, mas o package resolve)
# O flag -o (offline) garante que ele não tente conectar de novo, usando o que o go-offline baixou
RUN mvn clean package -DskipTests

# Estágio 2: Imagem Final (Execução) - Permanece igual
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]