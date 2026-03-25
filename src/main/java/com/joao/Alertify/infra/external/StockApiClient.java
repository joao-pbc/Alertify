package com.joao.Alertify.infra.external;

import com.joao.Alertify.domain.stock.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

/**
 * Adapter — translates the external Massive Stock API contract into domain objects,
 * isolating the rest of the application from third-party API changes.
 *
 * Endpoint used: GET /v3/reference/tickers
 */
@Slf4j
@Component
public class StockApiClient {

    private static final String TICKERS_PATH = "/v3/reference/tickers";
    private static final int DEFAULT_LIMIT = 100;

    private final WebClient stockWebClient;
    private final WebClient newsWebClient;
    private final String newsApiKey;

    public StockApiClient(
            @Qualifier("stockApiWebClient") WebClient stockWebClient,
            @Qualifier("newsApiWebClient") WebClient newsWebClient,
            @Value("${app.news-api.key}") String newsApiKey) {
        this.stockWebClient = stockWebClient;
        this.newsWebClient = newsWebClient;
        this.newsApiKey = newsApiKey;
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

    // ── News API ─────────────────────────────────────────────────────────────

    /**
     * Fetch latest news headlines for a given stock ticker.
     */
    public List<NewsApiArticle> fetchHeadlines(Stock stock) {
        try {
            NewsApiResponse response = newsWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/everything")
                            .queryParam("q", stock.getTicker())
                            .queryParam("apiKey", newsApiKey)
                            .queryParam("pageSize", "5")
                            .queryParam("language", "pt,en")
                            .queryParam("sortBy", "publishedAt")
                            .build())
                    .retrieve()
                    .bodyToMono(NewsApiResponse.class)
                    .block();

            return response != null && response.articles() != null
                    ? response.articles()
                    : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Erro ao buscar notícias para ticker '{}': {}", stock.getTicker(), e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Adapter (external DTO → domain model) ────────────────────────────────

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
}
