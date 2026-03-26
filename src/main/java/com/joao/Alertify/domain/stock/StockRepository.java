package com.joao.Alertify.domain.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByUserId(Long userId);

    @Query("SELECT s FROM Stock s JOIN FETCH s.user WHERE s.active = true")
    List<Stock> findByActiveTrueWithUser();

    List<Stock> findByActiveTrue();
    Optional<Stock> findByIdAndUserId(Long id, Long userId);
    boolean existsByTickerAndUserId(String ticker, Long userId);
}

