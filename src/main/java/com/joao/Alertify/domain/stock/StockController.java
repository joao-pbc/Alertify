package com.joao.Alertify.domain.stock;

import com.joao.Alertify.domain.user.User;
import com.joao.Alertify.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Returns all active tickers from the external Stock API (Massive).
     * GET /stocks/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StockDTO>>> fetchAll() {
        return ResponseEntity.ok(ApiResponse.ok(stockService.fetchAll()));
    }

    /**
     * Searches tickers by exact ticker symbol in the external Stock API.
     * GET /stocks/ticker?symbol=AAPL
     */
    @GetMapping("/ticker")
    public ResponseEntity<ApiResponse<List<StockDTO>>> searchByTicker(@RequestParam String symbol) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.searchByTicker(symbol)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDTO>>> listByUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.findAllByUser(user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockDTO>> create(
            @Valid @RequestBody StockRequest request,
            @AuthenticationPrincipal User user) {
        StockDTO created = stockService.save(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Ação adicionada", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        stockService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Ação removida", null));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<StockDTO>> toggleActive(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Status atualizado", stockService.toggleActive(id, user)));
    }
}

