package com.joao.Alertify.domain.stock;

import com.joao.Alertify.domain.user.User;
import com.joao.Alertify.infra.external.StockApiClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade — exposes a simple API to the controller, hiding StockApiClient,
 * StockRepository and business rules from the caller.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockApiClient stockApiClient;

    // ── Search external API ──────────────────────────────────────────────────

    /**
     * Returns all active tickers from the external Stock API.
     */
    public List<StockDTO> fetchAll() {
        return stockApiClient.fetchAllStocks()
                .stream()
                .map(StockDTO::fromExternal)
                .toList();
    }

    /**
     * Searches tickers by exact ticker symbol in the external Stock API.
     */
    public List<StockDTO> searchByTicker(String ticker) {
        return stockApiClient.searchByTicker(ticker)
                .stream()
                .map(StockDTO::fromExternal)
                .toList();
    }

    // ── User-owned CRUD ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StockDTO> findAllByUser(User user) {
        return stockRepository.findByUserId(user.getId())
                .stream()
                .map(StockDTO::from)
                .toList();
    }

    @Transactional
    public StockDTO save(StockRequest request, User user) {
        if (stockRepository.existsByTickerAndUserId(request.ticker(), user.getId())) {
            throw new IllegalArgumentException("Ticker já monitorado: " + request.ticker());
        }
        Stock stock = Stock.builder()
                .ticker(request.ticker().toUpperCase())
                .name(request.name())
                .exchange(request.exchange())
                .user(user)
                .build();
        return StockDTO.from(stockRepository.save(stock));
    }

    @Transactional
    public void delete(Long id, User user) {
        Stock stock = stockRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ação não encontrada: " + id));
        stockRepository.delete(stock);
    }

    @Transactional
    public StockDTO toggleActive(Long id, User user) {
        Stock stock = stockRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ação não encontrada: " + id));
        stock.setActive(!stock.isActive());
        return StockDTO.from(stockRepository.save(stock));
    }
}

