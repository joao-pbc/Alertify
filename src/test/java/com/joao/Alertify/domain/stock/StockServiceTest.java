package com.joao.Alertify.domain.stock;

import com.joao.Alertify.domain.user.User;
import com.joao.Alertify.infra.external.StockApiClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockApiClient stockApiClient;

    @InjectMocks
    private StockService stockService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").name("Test").build();
    }

    @Test
    @DisplayName("save() deve persistir ação e retornar DTO")
    void save_shouldPersistAndReturnDTO() {
        StockRequest request = new StockRequest("PETR4", "Petrobras", "BOVESPA");
        Stock saved = Stock.builder()
                .id(1L).ticker("PETR4").name("Petrobras").exchange("BOVESPA")
                .active(true).user(user).build();

        when(stockRepository.existsByTickerAndUserId("PETR4", 1L)).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenReturn(saved);

        StockDTO result = stockService.save(request, user);

        assertThat(result.ticker()).isEqualTo("PETR4");
        assertThat(result.active()).isTrue();
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("save() deve lançar exceção se ticker duplicado para o usuário")
    void save_shouldThrowWhenTickerAlreadyExists() {
        StockRequest request = new StockRequest("PETR4", "Petrobras", "BOVESPA");
        when(stockRepository.existsByTickerAndUserId("PETR4", 1L)).thenReturn(true);

        assertThatThrownBy(() -> stockService.save(request, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PETR4");
    }

    @Test
    @DisplayName("delete() deve lançar exceção quando ação não pertence ao usuário")
    void delete_shouldThrowWhenNotFound() {
        when(stockRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.delete(99L, user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findAllByUser() deve retornar lista de DTOs do usuário")
    void findAllByUser_shouldReturnUserStocks() {
        Stock stock = Stock.builder().id(1L).ticker("VALE3").user(user).active(true).build();
        when(stockRepository.findByUserId(1L)).thenReturn(List.of(stock));

        List<StockDTO> result = stockService.findAllByUser(user);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().ticker()).isEqualTo("VALE3");
    }

    @Test
    @DisplayName("toggleActive() deve inverter o status active da ação")
    void toggleActive_shouldFlipActiveStatus() {
        Stock stock = Stock.builder().id(1L).ticker("ITUB4").active(true).user(user).build();
        when(stockRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StockDTO result = stockService.toggleActive(1L, user);

        assertThat(result.active()).isFalse();
    }
}

