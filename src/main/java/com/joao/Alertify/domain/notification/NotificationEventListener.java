package com.joao.Alertify.domain.notification;

import com.joao.Alertify.domain.news.NewsDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer Listener — reacts to NewsDetectedEvent and dispatches
 * notifications via the NotificationFactory. Decoupled from the publisher.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationFactory factory;

    @EventListener
    @Async
    public void onNewsDetected(NewsDetectedEvent event) {
        String msg = """
                📈 *Nova notícia — %s*
                %s
                🔗 %s
                """.formatted(
                event.stock().getTicker(),
                event.headline(),
                event.url()
        );

        String recipient = event.recipient();
        if (recipient == null || recipient.isBlank()) {
            log.warn("Nenhum destinatário configurado para stock {}. Notificação ignorada.",
                    event.stock().getTicker());
            return;
        }

        factory.get("TelegramNotifier").notify(msg, recipient);
    }
}

