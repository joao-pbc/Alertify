package com.joao.Alertify.domain.news;

import com.joao.Alertify.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Observer Subject — persists news and publishes NewsDetectedEvent
 * without coupling to any specific notifier.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processNews(Stock stock, String headline, String description,
                            String url, String recipientChatId) {
        if (newsRepository.existsByStockAndUrl(stock, url)) {
            log.debug("Notícia já processada para {}: {}", stock.getTicker(), url);
            return;
        }

        News news = News.builder()
                .stock(stock)
                .title(headline)
                .description(description)
                .url(url)
                .build();
        newsRepository.save(news);

        log.info("Nova notícia detectada para {}: {}", stock.getTicker(), headline);
        eventPublisher.publishEvent(
                new NewsDetectedEvent(stock, headline, description, url, recipientChatId)
        );
    }

    @Transactional(readOnly = true)
    public List<News> findByStock(Stock stock) {
        return newsRepository.findByStockOrderByFetchedAtDesc(stock);
    }
}

