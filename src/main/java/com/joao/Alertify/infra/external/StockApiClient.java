package com.joao.Alertify.infra.external;

import com.joao.Alertify.domain.stock.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

/**
 * Adapter — translates the external Massive Stock API contract into domain objects,
 * isolating the rest of the application from third-party API changes.
 *
 * Endpoints used:
 *   GET /v3/reference/tickers  → stock reference data
 *   GET /v2/reference/news     → news articles per ticker
 */
@Slf4j
@Component
public class StockApiClient {

    private static final String TICKERS_PATH = "/v3/reference/tickers";
    private static final String NEWS_PATH     = "/v2/reference/news";
    private static final int    DEFAULT_LIMIT = 100;
    private static final int    NEWS_LIMIT    = 5;
    private static final String DEFAULT_SORT  = "published_utc";
    private static final String DEFAULT_ORDER = "desc";

    private final WebClient stockWebClient;

    public StockApiClient(
            @Qualifier("stockApiWebClient") WebClient stockWebClient) {
        this.stockWebClient = stockWebClient;
    }

    // ── Stock API (Massive) ──────────────────────────────────────────────────

    /**
     * Retrieves all active tickers from the Massive API.
     * Maps: GET /v3/reference/tickers?active=true&limit=100
     */
    public List<Stock> fetchAllStocks() {
        try {
            MassiveTickerResponse response = stockWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(TICKERS_PATH)
                            .queryParam("active", "true")
                            .queryParam("limit", DEFAULT_LIMIT)
                            .build())
                    .retrieve()
                    .bodyToMono(MassiveTickerResponse.class)
                    .block();

            return adaptFromMassive(response);
        } catch (Exception e) {
            log.warn("Erro ao listar todos os tickers da Massive API: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Searches tickers by exact ticker symbol using the Massive API.
     * Maps: GET /v3/reference/tickers?ticker={ticker}&active=true
     */
    public List<Stock> searchByTicker(String ticker) {
        try {
            MassiveTickerResponse response = stockWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(TICKERS_PATH)
                            .queryParam("ticker", ticker.toUpperCase())
                            .queryParam("active", "true")
                            .build())
                    .retrieve()
                    .bodyToMono(MassiveTickerResponse.class)
                    .block();

            return adaptFromMassive(response);
        } catch (Exception e) {
            log.warn("Erro ao buscar ticker '{}' na Massive API: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── News API (Massive) ───────────────────────────────────────────────────

    /**
     * Fetch latest news for a given stock ticker using the Massive News API.
     * Maps: GET /v2/reference/news?ticker={ticker}&sort=published_utc&order=desc&limit=5
     *
     * The ticker filter ensures only articles related to the stock are returned.
     * Results are sorted by published_utc descending so the freshest news comes first.
     */
    public List<NewsApiArticle> fetchHeadlines(Stock stock) {
        try {
            MassiveNewsResponse response = stockWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(NEWS_PATH)
                            .queryParam("ticker", stock.getTicker().toUpperCase())
                            .queryParam("sort",   DEFAULT_SORT)
                            .queryParam("order",  DEFAULT_ORDER)
                            .queryParam("limit",  NEWS_LIMIT)
                            .build())
                    .retrieve()
                    .bodyToMono(MassiveNewsResponse.class)
                    .block();

            return adaptFromMassiveNews(response);
        } catch (Exception e) {
            log.warn("Erro ao buscar notícias para ticker '{}': {}", stock.getTicker(), e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Adapters (external DTO → domain model) ───────────────────────────────

    private List<Stock> adaptFromMassive(MassiveTickerResponse response) {
        if (response == null || response.results() == null) {
            return Collections.emptyList();
        }
        return response.results().stream()
                .map(r -> Stock.builder()
                        .ticker(r.ticker() != null ? r.ticker() : "N/A")
                        .name(r.name())
                        .exchange(r.primaryExchange())
                        .build())
                .toList();
    }

    private List<NewsApiArticle> adaptFromMassiveNews(MassiveNewsResponse response) {
        if (response == null || response.results() == null) {
            return Collections.emptyList();
        }
        return response.results().stream()
                .map(r -> new NewsApiArticle(
                        r.title(),
                        r.description(),
                        r.articleUrl(),
                        r.publishedUtc(),
                        r.publisher() != null ? r.publisher().name() : null,
                        r.tickers(),
                        extractSentiment(r, stock -> null)
                ))
                .toList();
    }

    /**
     * Extracts the primary sentiment from the insights list.
     * Returns the sentiment of the first insight if available, null otherwise.
     */
    private String extractSentiment(MassiveNewsResponse.NewsResult result, java.util.function.Function<MassiveNewsResponse.NewsResult, String> fallback) {
        if (result.insights() != null && !result.insights().isEmpty()) {
            return result.insights().get(0).sentiment();
        }
        return fallback.apply(result);
    }
}
