package com.joao.Alertify.domain.notification;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Factory Method — Spring injects ALL NotificationService implementations.
 * Returns the correct notifier based on the channel name (class simple name).
 * Adding a new channel requires only a new @Component — no factory change needed.
 */
@Component
public class NotificationFactory {

    private final Map<String, NotificationService> notifiers;

    public NotificationFactory(List<NotificationService> services) {
        this.notifiers = services.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getSimpleName(),
                        s -> s
                ));
    }

    public NotificationService get(String channel) {
        return Optional.ofNullable(notifiers.get(channel))
                .orElseThrow(() -> new IllegalArgumentException("Canal de notificação inválido: " + channel));
    }

    public Map<String, NotificationService> available() {
        return Map.copyOf(notifiers);
    }
}

