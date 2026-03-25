package com.joao.Alertify.domain.news;

import com.joao.Alertify.domain.stock.Stock;
import com.joao.Alertify.domain.stock.StockRepository;
import com.joao.Alertify.infra.external.NewsApiArticle;
import com.joao.Alertify.infra.external.StockApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Template Method — defines a fixed polling skeleton:
 *   1. Fetch active stocks
 *   2. For each stock, fetch headlines (overridable step)
 *   3. Filter already-seen articles
 *   4. Publish NewsDetectedEvent via NewsService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsFetcherJob {

    private final StockRepository stockRepository;
    private final NewsService newsService;
    private final StockApiClient stockApiClient;

    @Scheduled(fixedDelayString = "${app.scheduler.interval-ms:300000}")
    public void run() {
        List<Stock> activeStocks = stockRepository.findByActiveTrue();
        log.info("Scheduler executando — {} ações ativas monitoradas", activeStocks.size());
        activeStocks.forEach(this::checkNews);
    }

    // Template skeleton — can be extended for different news sources
    private void checkNews(Stock stock) {
        List<NewsApiArticle> headlines = fetchHeadlines(stock);
        headlines.forEach(article -> {
            if (isNew(stock, article)) {
                String recipient = stock.getUser() != null
                        ? stock.getUser().getTelegramChatId()
                        : null;
                newsService.processNews(
                        stock,
                        article.title(),
                        article.description(),
                        article.url(),
                        recipient
                );
            }
        });
    }

    // Overridable — different implementations can provide different news sources
    protected List<NewsApiArticle> fetchHeadlines(Stock stock) {
        return stockApiClient.fetchHeadlines(stock);
    }

    private boolean isNew(Stock stock, NewsApiArticle article) {
        return article.url() != null
                && !article.url().isBlank()
                && !newsService.findByStock(stock)
                .stream()
                .anyMatch(n -> n.getUrl().equals(article.url()));
    }
}

