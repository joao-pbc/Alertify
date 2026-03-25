package com.joao.Alertify.domain.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByUserId(Long userId);
    List<Stock> findByActiveTrue();
    Optional<Stock> findByIdAndUserId(Long id, Long userId);
    boolean existsByTickerAndUserId(String ticker, Long userId);
}

