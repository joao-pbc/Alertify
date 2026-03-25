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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StockDTO>>> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.search(query)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDTO>>> list(@AuthenticationPrincipal User user) {
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

