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
 * Adapter — translates the external Stock/News API contract into domain objects,
 * isolating the rest of the application from third-party API changes.
 */
@Slf4j
@Component
public class StockApiClient {

    private final WebClient webClient;
    private final String apiKey;

    public StockApiClient(
            @Qualifier("newsApiWebClient") WebClient webClient,
            @Value("${app.news-api.key}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    /**
     * Search for stocks by query — adapts external DTO → domain model.
     */
    public List<Stock> searchStocks(String query) {
        try {
            List<StockApiResponse> responses = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/everything")
                            .queryParam("q", query)
                            .queryParam("apiKey", apiKey)
                            .queryParam("pageSize", "5")
                            .build())
                    .retrieve()
                    .bodyToFlux(StockApiResponse.class)
                    .collectList()
                    .block();

            return adapt(responses == null ? Collections.emptyList() : responses);
        } catch (Exception e) {
            log.warn("Erro ao consultar API externa para query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch latest news headlines for a given stock ticker.
     */
    public List<NewsApiArticle> fetchHeadlines(Stock stock) {
        try {
            NewsApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/everything")
                            .queryParam("q", stock.getTicker())
                            .queryParam("apiKey", apiKey)
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

    // Adapter: converts external DTO → domain Stock (without persisting)
    private List<Stock> adapt(List<StockApiResponse> responses) {
        return responses.stream()
                .map(r -> Stock.builder()
                        .ticker(r.ticker() != null ? r.ticker() : "N/A")
                        .name(r.name())
                        .exchange(r.exchange())
                        .build())
                .toList();
    }
}

