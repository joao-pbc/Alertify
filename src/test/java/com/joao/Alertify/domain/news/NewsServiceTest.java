package com.joao.Alertify.domain.news;

import com.joao.Alertify.domain.stock.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("processNews() deve persistir e publicar evento para notícia nova")
    void processNews_shouldSaveAndPublishEvent() {
        Stock stock = Stock.builder().id(1L).ticker("PETR4").build();
        when(newsRepository.existsByStockAndUrl(stock, "http://news.com/1")).thenReturn(false);

        newsService.processNews(stock, "Headline", "Desc", "http://news.com/1", "12345");

        verify(newsRepository).save(any(News.class));

        ArgumentCaptor<NewsDetectedEvent> captor = ArgumentCaptor.forClass(NewsDetectedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NewsDetectedEvent event = captor.getValue();
        assertThat(event.headline()).isEqualTo("Headline");
        assertThat(event.stock().getTicker()).isEqualTo("PETR4");
        assertThat(event.recipient()).isEqualTo("12345");
    }

    @Test
    @DisplayName("processNews() não deve publicar evento para notícia já existente")
    void processNews_shouldSkipDuplicateNews() {
        Stock stock = Stock.builder().id(1L).ticker("VALE3").build();
        when(newsRepository.existsByStockAndUrl(stock, "http://dup.com")).thenReturn(true);

        newsService.processNews(stock, "Dup", "Desc", "http://dup.com", "99999");

        verify(newsRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("findByStock() deve retornar notícias do repositório")
    void findByStock_shouldReturnNews() {
        Stock stock = Stock.builder().id(1L).ticker("ITUB4").build();
        News news = News.builder().id(1L).stock(stock).title("Test").url("http://t.com").build();
        when(newsRepository.findByStockOrderByFetchedAtDesc(stock)).thenReturn(List.of(news));

        List<News> result = newsService.findByStock(stock);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Test");
    }
}

