# Stock & News API Integration Flow — Massive API

## Visão Geral

Este documento descreve o fluxo de integração com a **Massive API** para busca de stocks (tickers) e notícias financeiras.

---

## Arquitetura

```
HTTP Request
    │
    ▼
StockController          StockApiClient (Adapter)
    │                         │
    │  fetchAllStocks()        │── GET /v3/reference/tickers?active=true&limit=100
    │  searchByTicker(ticker)  │── GET /v3/reference/tickers?ticker={TICKER}&active=true
    │                         │
    └─── StockService ────────►│
                               │
NewsFetcherJob (Scheduler)     │
    │                          │
    │  fetchHeadlines(stock)   │── GET /v2/reference/news?ticker={TICKER}
    │                          │       &sort=published_utc
    └─────────────────────────►│       &order=desc
                               │       &limit=5
```

---

## Fluxo de Implementação

### 1. Busca de Tickers (`/v3/reference/tickers`)

#### `fetchAllStocks()`
- **Endpoint:** `GET /v3/reference/tickers?active=true&limit=100`
- **Auth:** `Bearer token` via header `Authorization` (configurado no `WebClientConfig`)
- **Response DTO:** `MassiveTickerResponse` → `List<TickerResult>`
- **Adapter:** `adaptFromMassive()` → mapeia `ticker`, `name`, `primary_exchange` para `Stock`

#### `searchByTicker(String ticker)`
- **Endpoint:** `GET /v3/reference/tickers?ticker={TICKER}&active=true`
- **Nota:** o ticker é sempre enviado em **uppercase**
- **Response DTO:** `MassiveTickerResponse`
- **Adapter:** mesmo `adaptFromMassive()` acima

---

### 2. Busca de Notícias (`/v2/reference/news`)

#### `fetchHeadlines(Stock stock)`
- **Endpoint:** `GET /v2/reference/news?ticker={TICKER}&sort=published_utc&order=desc&limit=5`
- **Auth:** mesmo `Bearer token` do `stockApiWebClient`
- **Parâmetros:**

| Parâmetro | Valor           | Descrição                              |
|-----------|-----------------|----------------------------------------|
| `ticker`  | ex: `AAPL`      | Filtra artigos pelo símbolo do ticker  |
| `sort`    | `published_utc` | Campo de ordenação                     |
| `order`   | `desc`          | Mais recentes primeiro                 |
| `limit`   | `5`             | Máximo de artigos retornados           |

- **Response DTO:** `MassiveNewsResponse` → `List<NewsResult>`
- **Adapter:** `adaptFromMassiveNews()` → mapeia para `NewsApiArticle`

---

## DTOs Envolvidos

### `MassiveNewsResponse`
Mapeia o envelope JSON da Massive News API:
```json
{
  "count": 1,
  "next_url": "...",
  "request_id": "...",
  "status": "OK",
  "results": [
    {
      "id": "...",
      "title": "...",
      "description": "...",
      "article_url": "...",
      "published_utc": "2024-06-24T18:33:53Z",
      "publisher": { "name": "Investing.com", ... },
      "tickers": ["UBS"],
      "insights": [{ "ticker": "UBS", "sentiment": "positive", "sentiment_reasoning": "..." }]
    }
  ]
}
```

### `NewsApiArticle`
DTO de saída do adapter (domínio interno):
```java
record NewsApiArticle(
    String title,
    String description,
    String url,           // ← article_url da Massive
    String publishedAt,   // ← published_utc da Massive
    String publisher,     // ← publisher.name da Massive
    List<String> tickers,
    String sentiment      // ← insights[0].sentiment (se houver)
)
```

---

## Fluxo do Scheduler (`NewsFetcherJob`)

```
@Scheduled (every 5 min)
    │
    ├── stockRepository.findByActiveTrue()
    │       └── Para cada Stock ativo:
    │
    ├── StockApiClient.fetchHeadlines(stock)
    │       └── GET /v2/reference/news?ticker={ticker}&sort=published_utc&order=desc&limit=5
    │
    ├── Para cada NewsApiArticle retornado:
    │       ├── isNew? (verifica se URL já existe no banco)
    │       └── NewsService.processNews(...)
    │               ├── Persiste News no banco
    │               └── Publica NewsDetectedEvent
    │                       └── NotificationEventListener → TelegramNotifier
```

---

## Autenticação

| Cliente WebClient   | Tipo de Auth             | Configuração                           |
|---------------------|--------------------------|----------------------------------------|
| `stockApiWebClient` | `Bearer {STOCK_TOKEN}`   | `app.stock-api.token` (env var)        |
| `newsApiWebClient`  | ~~`apiKey` query param~~ | **Removido** — news agora via Massive  |

> **Nota:** com a migração para a Massive News API, **não há mais um WebClient separado para news**.
> Tanto tickers quanto notícias são servidos pelo mesmo `stockApiWebClient`, autenticado via `Bearer token`.
> A chave `app.news-api.key` e o bean `newsApiWebClient` não são mais utilizados pelo `StockApiClient`.

---

## Arquivos Modificados / Criados

| Arquivo                                          | Ação           | Descrição                                                                         |
|--------------------------------------------------|----------------|-----------------------------------------------------------------------------------|
| `infra/external/MassiveNewsResponse.java`        | ✅ Criado       | DTO mapeando o envelope `/v2/reference/news`                                      |
| `infra/external/NewsApiArticle.java`             | ✏️ Atualizado  | Campos alinhados com a Massive (`publisher` como `String`, `tickers`, `sentiment`)|
| `infra/external/StockApiClient.java`             | ✏️ Atualizado  | `fetchHeadlines()` usa `stockWebClient` com Massive News API (`ticker`, `sort`, `order`, `limit`) |
