# 📈 Alertify — Stock News Alert API

> **REST API** que monitora ações da bolsa e envia notificações em tempo real via **Telegram** sempre que novas notícias são detectadas para os ativos monitorados pelo usuário.

---

## 🧭 Visão Geral

O **Alertify** é um projeto back-end construído com **Spring Boot 4 + Java 21** que resolve um problema real do mercado financeiro: acompanhar notícias relevantes sobre ativos sem precisar ficar atualizando portais manualmente.

O fluxo central é simples:
1. O usuário se cadastra e associa seu **Telegram Chat ID**
2. Ele adiciona os **tickers** (ex: `AAPL`, `PETR4`) que deseja monitorar
3. Um **job agendado** consulta a API de notícias a cada 5 minutos
4. Ao detectar uma notícia nova, o sistema publica um **evento de domínio** que dispara uma mensagem no Telegram do usuário

---

## ✨ Funcionalidades

### 🔐 Autenticação & Segurança
- Registro de usuário com validação de e-mail, senha e nome
- Login com e-mail e senha — retorna **JWT Bearer Token**
- Proteção stateless com **Spring Security + JWT** (JJWT 0.12.6)
- Senhas armazenadas com **BCrypt**
- Todas as rotas (exceto `/auth/**`) exigem token válido

### 📊 Gestão de Ações (Stocks)
| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/stocks/all` | Lista todos os tickers ativos da API externa |
| `GET` | `/stocks/ticker?symbol=AAPL` | Busca ticker por símbolo |
| `GET` | `/stocks` | Lista ações monitoradas pelo usuário autenticado |
| `POST` | `/stocks` | Adiciona um ticker para monitoramento |
| `DELETE` | `/stocks/{id}` | Remove um ticker da watchlist |
| `PATCH` | `/stocks/{id}/toggle` | Ativa/desativa monitoramento de um ticker |

### 📰 Monitoramento de Notícias
- **Job agendado** (padrão: a cada 5 minutos) varre todos os tickers ativos
- Integração com **Massive Stock API** para dados de tickers e notícias
- Deduplicação por URL — cada notícia é processada uma única vez
- Histórico de notícias armazenado por ativo no banco de dados

### 🔔 Sistema de Notificações
- Notificação via **Bot do Telegram** com formatação Markdown
- Arquitetura extensível: adicionar novos canais (e-mail, Slack) requer apenas um novo `@Component`
- Processamento **assíncrono** dos eventos de notificação
- Tratamento de erros HTTP 4xx/5xx da API do Telegram com logs estruturados

---

## 🏗️ Arquitetura & Design Patterns

O projeto aplica boas práticas de design orientado a domínio e padrões de projeto:

| Pattern | Onde é aplicado |
|---------|----------------|
| **Template Method** | `NewsFetcherJob` — skeleton fixo de polling, step de busca sobrescrevível |
| **Factory Method** | `NotificationFactory` — resolve o canal correto por nome de classe |
| **Adapter** | `StockApiClient` — isola o contrato da API externa do domínio |
| **Facade** | `StockService` — simplifica a interface entre Controller e infra |
| **Observer (Spring Events)** | `NewsDetectedEvent` / `NotificationEventListener` — desacopla detecção de notificação |

### Estrutura de Pacotes
```
com.joao.Alertify
├── config/          # Beans de configuração (Security, WebClient)
├── domain/
│   ├── auth/        # Registro, login, JWT
│   ├── news/        # Entidade, job agendado, repositório
│   ├── notification/ # Factory, listener, notificador Telegram
│   ├── stock/       # Entidade, CRUD, integração API externa
│   └── user/        # Entidade User, roles
├── infra/
│   ├── external/    # Clientes HTTP (Massive API, News API)
│   └── security/    # JwtAuthFilter, JwtTokenProvider
└── shared/
    ├── dto/         # ApiResponse padronizado
    └── exception/   # Handlers globais
```

---

## 🛠️ Stack Tecnológica

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 |
| Segurança | Spring Security + JWT (JJWT 0.12.6) |
| Persistência | Spring Data JPA + PostgreSQL 17 |
| Migrations | Flyway |
| HTTP Client | Spring WebFlux (WebClient) |
| Notificação | Telegram Bots API 6.9.7 |
| Build | Maven (wrapper incluído) |
| Container | Docker (multi-stage build) |
| Util | Lombok |

---

## ⚙️ Configuração — Variáveis de Ambiente

Crie um arquivo **`.env`** na raiz do projeto com as seguintes variáveis:

```env
# ── Banco de Dados ────────────────────────────────────────────────────────────
DB_URL=jdbc:postgresql://postgres:5432/mydatabase
DB_USERNAME=myuser
DB_PASSWORD=secret

# ── JWT ───────────────────────────────────────────────────────────────────────
# Chave secreta (mínimo 256 bits / 32 chars). Gere com: openssl rand -base64 64
JWT_SECRET=sua_chave_secreta_muito_longa_e_segura_aqui
JWT_EXPIRATION_MS=86400000   # 24 horas (em milissegundos)

# ── Telegram Bot ──────────────────────────────────────────────────────────────
# Crie seu bot em https://t.me/BotFather e obtenha o token
TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrSTUvwxYZ
TELEGRAM_BOT_USERNAME=SeuBotUsername_bot

# ── News API ──────────────────────────────────────────────────────────────────
# Cadastre-se em https://newsapi.org para obter a chave
NEWS_API_KEY=sua_news_api_key_aqui

# ── Stock API (Massive / Polygon.io-compatible) ───────────────────────────────
# Token de acesso à API de dados de mercado
STOCK_TOKEN=seu_stock_api_token_aqui
```

> **Nota de segurança:** o arquivo `.env` já está no `.gitignore` — nunca o comite no repositório.

### Como obter as credenciais

| Variável | Como obter |
|----------|-----------|
| `TELEGRAM_BOT_TOKEN` | Converse com [@BotFather](https://t.me/BotFather) no Telegram → `/newbot` |
| `TELEGRAM_BOT_USERNAME` | Nome escolhido na criação do bot (ex: `MeuAlertBot_bot`) |
| `NEWS_API_KEY` | Cadastro gratuito em [newsapi.org](https://newsapi.org) |
| `STOCK_TOKEN` | Token da sua conta na API de mercado (Massive/Polygon-compatible) |
| `JWT_SECRET` | Gere localmente: `openssl rand -base64 64` |

---

## 🚀 Como Rodar

### Opção 1 — Docker Compose (Recomendado)

Sobe a aplicação e o PostgreSQL juntos, sem precisar instalar nada além do Docker.

**Pré-requisitos:** Docker Desktop instalado e em execução.

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/Alertify.git
cd Alertify

# 2. Crie o arquivo .env na raiz (veja seção acima)
# Exemplo rápido:
copy .env.example .env   # Windows
# cp .env.example .env   # Linux/macOS

# 3. Suba os containers
docker compose up --build

# A API estará disponível em: http://localhost:8080
```

Para rodar em background:
```bash
docker compose up --build -d

# Acompanhe os logs
docker compose logs -f app

# Pare os containers
docker compose down
```

---

### Opção 2 — Execução Local (Maven)

**Pré-requisitos:** Java 21+, Maven (ou use o wrapper `mvnw`) e PostgreSQL rodando localmente.

```bash
# 1. Suba o banco de dados (via Docker, mais prático)
docker compose up postgres -d

# 2. Exporte as variáveis de ambiente
# Windows (PowerShell):
$env:DB_URL="jdbc:postgresql://localhost:5432/mydatabase"
$env:DB_USERNAME="myuser"
$env:DB_PASSWORD="secret"
$env:JWT_SECRET="sua_chave_secreta_aqui"
$env:TELEGRAM_BOT_TOKEN="seu_token"
$env:TELEGRAM_BOT_USERNAME="SeuBot_bot"
$env:NEWS_API_KEY="sua_key"
$env:STOCK_TOKEN="seu_token"

# 3. Execute a aplicação
./mvnw spring-boot:run      # Linux/macOS
mvnw.cmd spring-boot:run    # Windows
```

---

### Verificar se está funcionando

```bash
# Health check básico
curl http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","password":"123456"}'

# Resposta esperada:
# {"status":"ok","message":"Usuário criado com sucesso","data":{"token":"eyJ..."}}
```

---

## 📋 Exemplos de Uso da API

### Registrar usuário
```http
POST /auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "minhasenha123",
  "telegramChatId": "123456789"
}
```
> **Dica:** Para obter seu `telegramChatId`, envie uma mensagem para [@userinfobot](https://t.me/userinfobot) no Telegram.

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "minhasenha123"
}
```

### Adicionar ação para monitorar
```http
POST /stocks
Authorization: Bearer {seu_token_jwt}
Content-Type: application/json

{
  "ticker": "AAPL",
  "name": "Apple Inc.",
  "exchange": "NASDAQ"
}
```

### Buscar ticker na API externa
```http
GET /stocks/ticker?symbol=AAPL
Authorization: Bearer {seu_token_jwt}
```

---

## 🗄️ Banco de Dados

As migrations são executadas automaticamente pelo **Flyway** na inicialização.

| Migration | Descrição |
|-----------|-----------|
| `V1__create_users_table.sql` | Tabela de usuários com roles e Telegram Chat ID |
| `V2__create_stocks_table.sql` | Watchlist de ações por usuário (unique por ticker+user) |
| `V3__create_news_table.sql` | Histórico de notícias por ativo (deduplicação por URL) |
| `V4__add_telegram_chat_id_to_users.sql` | Adiciona coluna de Chat ID ao usuário |

---

## 🧪 Testes

```bash
# Executa todos os testes
./mvnw test          # Linux/macOS
mvnw.cmd test        # Windows

# Build sem testes (mais rápido para desenvolvimento)
./mvnw package -DskipTests
```

---

## 🐳 Dockerfile — Multi-Stage Build

O `Dockerfile` utiliza **build em dois estágios** para produzir uma imagem final enxuta e segura:

| Stage | Base Image | Responsabilidade |
|-------|-----------|-----------------|
| `builder` | `eclipse-temurin:21-jdk-alpine` | Compila o código e gera o JAR |
| `runtime` | `eclipse-temurin:21-jre-alpine` | Executa apenas o JAR (sem JDK, sem Maven) |

**Destaques de segurança do container:**
- Imagens Alpine (menor superfície de ataque)
- **Usuário não-root** (`alertify`) em runtime
- Cache inteligente de layers Docker (dependências separadas do código-fonte)

---

## 📁 Estrutura do Projeto

```
Alertify/
├── src/
│   ├── main/
│   │   ├── java/com/joao/Alertify/
│   │   │   ├── config/          # SecurityConfig, WebClientConfig
│   │   │   ├── domain/
│   │   │   │   ├── auth/        # AuthController, AuthService, JWT
│   │   │   │   ├── news/        # News entity, NewsFetcherJob (scheduler)
│   │   │   │   ├── notification/ # TelegramNotifier, Factory, EventListener
│   │   │   │   ├── stock/       # StockController, StockService, StockApiClient
│   │   │   │   └── user/        # User entity, UserRepository
│   │   │   ├── infra/
│   │   │   │   ├── external/    # HTTP clients para APIs externas
│   │   │   │   └── security/    # JwtAuthFilter, JwtTokenProvider
│   │   │   └── shared/
│   │   │       ├── dto/         # ApiResponse<T> padronizado
│   │   │       └── exception/   # GlobalExceptionHandler
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Flyway SQL scripts
├── compose.yaml                 # Docker Compose (app + postgres)
├── Dockerfile                   # Multi-stage build
├── pom.xml
└── .env                         # ⚠️ Não comitar — variáveis sensíveis
```

---

## 🔒 Segurança

- **Stateless:** nenhuma sessão HTTP é mantida no servidor
- **JWT:** tokens com expiração configurável (padrão 24h)
- **BCrypt:** hashing adaptativo de senhas
- **Validação de entrada:** Bean Validation em todos os endpoints
- **Container não-root:** processo Java roda sem privilégios de root
- **Secrets via variáveis de ambiente:** nenhuma credencial hardcoded no código

---

*Desenvolvido por João — projeto portfólio demonstrando Spring Boot, segurança REST, event-driven design e integração com APIs externas.*

