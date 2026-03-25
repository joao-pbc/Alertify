# 📈 Alertify — Fluxo de Integração com a Stock API (Massive)

## Visão Geral

Este documento descreve o fluxo de implementação da integração com a **Massive Stock API** para listagem e pesquisa de tickers no Alertify.

---

## 🗂️ Arquivos Modificados / Criados

| Arquivo | Tipo | Ação |
|---|---|---|
| `application.properties` | Config | Adicionadas propriedades `app.stock-api.base-url` e `app.stock-api.token` |
| `WebClientConfig.java` | Config | Novo bean `stockApiWebClient` com autenticação Bearer |
| `MassiveTickerResponse.java` | DTO externo | **Criado** — mapeia JSON da Massive API (`results[]`, `count`, `status`) |
| `StockApiClient.java` | Adapter | Refatorado — usa `stockWebClient` com 3 métodos: `fetchAllStocks`, `searchByTicker`, `searchStocks` |
| `StockDTO.java` | DTO de domínio | Adicionado `fromExternal()` para stocks sem persistência |
| `StockService.java` | Service | Adicionados `fetchAll()` e `searchByTicker()` |
| `StockController.java` | Controller | Adicionados endpoints `GET /stocks/all` e `GET /stocks/ticker?symbol=` |

---

## 🔄 Fluxo Completo (por camada)

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────┐
│              StockController                    │
│                                                 │
│  GET /stocks/all          → fetchAll()          │
│  GET /stocks/ticker?symbol → searchByTicker()   │
│  GET /stocks/search?query  → search()           │
└────────────────────┬────────────────────────────┘
                     │ delega para
                     ▼
┌─────────────────────────────────────────────────┐
│              StockService (Facade)              │
│                                                 │
│  fetchAll()        → stockApiClient.fetchAllStocks()   │
│  searchByTicker()  → stockApiClient.searchByTicker()   │
│  search()          → stockApiClient.searchStocks()     │
│                                                 │
│  Mapeia: Stock → StockDTO.fromExternal()        │
└────────────────────┬────────────────────────────┘
                     │ delega para
                     ▼
┌─────────────────────────────────────────────────┐
│           StockApiClient (Adapter)              │
│                                                 │
│  Realiza GET /v3/reference/tickers via WebClient│
│  Deserializa → MassiveTickerResponse            │
│  Adapta:  TickerResult → Stock (domínio)        │
└────────────────────┬────────────────────────────┘
                     │ HTTP via
                     ▼
┌─────────────────────────────────────────────────┐
│        stockApiWebClient (WebClient Bean)       │
│                                                 │
│  Base URL: https://api.massive.com              │
│  Header:   Authorization: Bearer <STOCK_TOKEN>  │
│  Header:   Accept: application/json             │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
          Massive Stock API externa
```

---

## 📡 Endpoints Disponíveis

### `GET /stocks/all`
Retorna todos os tickers ativos cadastrados na Massive API.

**Query Params internos (Massive):** `active=true&limit=100`

**Exemplo de resposta:**
```json
{
  "status": "OK",
  "data": [
    {
      "id": null,
      "ticker": "AAPL",
      "name": "Apple Inc.",
      "exchange": "XNAS",
      "active": true,
      "createdAt": null
    }
  ]
}
```

---

### `GET /stocks/ticker?symbol=AAPL`
Busca um ticker exato na Massive API.

**Query Params internos (Massive):** `ticker=AAPL&active=true`

**Exemplo de resposta:**
```json
{
  "status": "OK",
  "data": [
    {
      "id": null,
      "ticker": "AAPL",
      "name": "Apple Inc.",
      "exchange": "XNAS",
      "active": true,
      "createdAt": null
    }
  ]
}
```

---

### `GET /stocks/search?query=apple`
Busca tickers por texto livre (nome ou símbolo).

**Query Params internos (Massive):** `search=apple&active=true&limit=20`

---

## 🏗️ Decisões de Design

### 1. Padrão Adapter (`StockApiClient`)
O `StockApiClient` é o único ponto de contato com a API externa. Toda a tradução do contrato externo (`MassiveTickerResponse`) para o modelo de domínio (`Stock`) acontece **dentro do adapter**, via o método privado `adaptFromMassive()`. O resto da aplicação nunca conhece a estrutura JSON da Massive.

### 2. Separação de DTOs
| DTO | Propósito |
|---|---|
| `MassiveTickerResponse` | Mapeia o JSON bruto da API (`@JsonIgnoreProperties(ignoreUnknown = true)`) |
| `Stock` (domínio) | Modelo de domínio — pode ou não estar persistido |
| `StockDTO` | Resposta da API REST do Alertify (contrato de saída) |

### 3. `StockDTO.fromExternal()` vs `StockDTO.from()`
- `from(Stock)` → para stocks **persistidos** (com `id`, `user`, `createdAt`)
- `fromExternal(Stock)` → para stocks **transientes** vindos da API externa (`id=null`, `createdAt=null`)

### 4. WebClient por responsabilidade
Dois beans distintos evitam acoplamento de configuração:
- `stockApiWebClient` → Massive API (Bearer token, base URL própria)
- `newsApiWebClient` → NewsAPI (API Key por query param, base URL própria)

### 5. Autenticação Bearer via Header padrão
O token `STOCK_TOKEN` é lido do `.env` e injetado no `WebClient` como `defaultHeader("Authorization", "Bearer ...")`. Isso garante que **todas as requisições** ao client já estejam autenticadas sem repetir o header em cada chamada.

### 6. Tratamento de erros no Adapter
Cada método do `StockApiClient` possui um `try/catch` com `log.warn`. Em caso de falha (timeout, 4xx, 5xx), retorna `Collections.emptyList()` sem propagar exceção para o controller — garantindo resiliência.

---

## ⚙️ Configuração necessária no `.env`

```dotenv
STOCK_TOKEN=seu_token_aqui
```

E em `application.properties`:
```properties
app.stock-api.base-url=https://api.massive.com
app.stock-api.token=${STOCK_TOKEN}
```

---

## 🔑 Mapeamento JSON → Domínio

| Campo Massive API (`TickerResult`) | Campo domínio (`Stock`) |
|---|---|
| `ticker` | `ticker` |
| `name` | `name` |
| `primary_exchange` | `exchange` |
| *(não mapeado)* | `active = true` (padrão) |
| *(não mapeado)* | `id = null` (não persistido) |
| *(não mapeado)* | `user = null` (não persistido) |

---

## ✅ Critério de Aceitação

| Critério | Status |
|---|---|
| Fazer requisição para a API Massive via WebClient autenticado | ✅ |
| Tratar o JSON de resposta no adapter (`MassiveTickerResponse`) | ✅ |
| Retornar lista de stocks via `GET /stocks/all` | ✅ |
| Pesquisar por ticker exato via `GET /stocks/ticker?symbol=` | ✅ |
| Isolar contrato externo do domínio via Adapter pattern | ✅ |

