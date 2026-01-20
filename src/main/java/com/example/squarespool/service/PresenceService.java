package com.example.squarespool.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    private final Map<Long, Set<String>> boardSessions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void addSession(Long boardId, String sessionId) {
        boardSessions.compute(boardId, (key, existing) -> {
            Set<String> sessions = existing == null ? Collections.synchronizedSet(new HashSet<>()) : existing;
            sessions.add(sessionId);
            return sessions;
        });
        broadcast(boardId);
    }

    public void removeSession(String sessionId) {
        for (Map.Entry<Long, Set<String>> entry : boardSessions.entrySet()) {
            if (entry.getValue().remove(sessionId)) {
                broadcast(entry.getKey());
            }
        }
    }

    private void broadcast(Long boardId) {
        int count = boardSessions.getOrDefault(boardId, Set.of()).size();
        messagingTemplate.convertAndSend("/topic/boards/" + boardId + "/presence", count);
    }
}
