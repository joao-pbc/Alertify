# ─────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia apenas os arquivos de dependências primeiro (cache de layers)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Baixa as dependências sem compilar o código (aproveita cache do Docker)
RUN ./mvnw dependency:go-offline -q

# Copia o código-fonte e faz o build
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S alertify && adduser -S alertify -G alertify
USER alertify

# Copia apenas o JAR gerado no stage anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

