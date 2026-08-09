package com.lifelink.request;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import com.lifelink.webhook.dto.WebhookEventPayload;

@Slf4j
@Service
public class RequestSseService {

    private final Map<UUID, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID requestId, String subscriberId) {
        SseEmitter emitter = new SseEmitter(3600000L); // 1 hour timeout
        
        emitters.computeIfAbsent(requestId, k -> new ConcurrentHashMap<>())
                .put(subscriberId, emitter);

        emitter.onCompletion(() -> removeEmitter(requestId, subscriberId));
        emitter.onTimeout(() -> removeEmitter(requestId, subscriberId));
        emitter.onError(e -> removeEmitter(requestId, subscriberId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            removeEmitter(requestId, subscriberId);
        }

        return emitter;
    }

    public void dispatchEvent(UUID requestId, WebhookEventPayload payload) {
        Map<String, SseEmitter> requestEmitters = emitters.get(requestId);
        if (requestEmitters != null) {
            requestEmitters.forEach((subscriberId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().name("EVENT").data(payload));
                } catch (IOException e) {
                    removeEmitter(requestId, subscriberId);
                }
            });
        }
    }

    private void removeEmitter(UUID requestId, String subscriberId) {
        Map<String, SseEmitter> requestEmitters = emitters.get(requestId);
        if (requestEmitters != null) {
            requestEmitters.remove(subscriberId);
            if (requestEmitters.isEmpty()) {
                emitters.remove(requestId);
            }
        }
    }
}
