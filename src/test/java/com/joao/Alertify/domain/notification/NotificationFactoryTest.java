package com.joao.Alertify.domain.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationFactoryTest {

    private TelegramNotifier telegramNotifier;
    private NotificationFactory factory;

    @BeforeEach
    void setUp() {
        telegramNotifier = mock(TelegramNotifier.class);
        factory = new NotificationFactory(List.of(telegramNotifier));
    }

    @Test
    @DisplayName("get() deve retornar TelegramNotifier pelo nome da classe")
    void get_shouldReturnTelegramNotifier() {
        NotificationService result = factory.get("TelegramNotifier");
        assertThat(result).isSameAs(telegramNotifier);
    }

    @Test
    @DisplayName("get() deve lançar exceção para canal desconhecido")
    void get_shouldThrowForUnknownChannel() {
        assertThatThrownBy(() -> factory.get("WhatsAppNotifier"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WhatsAppNotifier");
    }
}

