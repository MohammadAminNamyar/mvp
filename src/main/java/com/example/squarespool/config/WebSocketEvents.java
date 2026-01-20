package com.example.squarespool.config;

import com.example.squarespool.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketEvents {
    private final PresenceService presenceService;

    public WebSocketEvents(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination != null && destination.contains("/topic/boards/")) {
            String[] parts = destination.split("/");
            if (parts.length >= 4) {
                try {
                    Long boardId = Long.parseLong(parts[3]);
                    presenceService.addSession(boardId, accessor.getSessionId());
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        presenceService.removeSession(event.getSessionId());
    }
}
