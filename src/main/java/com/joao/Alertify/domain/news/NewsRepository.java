package com.joao.Alertify.domain.news;

import com.joao.Alertify.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsByStockAndUrl(Stock stock, String url);
    List<News> findByStockOrderByFetchedAtDesc(Stock stock);
    List<News> findByNotifiedFalse();
}

