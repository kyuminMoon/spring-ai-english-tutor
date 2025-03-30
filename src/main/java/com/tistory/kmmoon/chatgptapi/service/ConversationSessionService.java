package com.tistory.kmmoon.chatgptapi.service;

import com.tistory.kmmoon.chatgptapi.session.ConversationSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 세션 관리 서비스
 */
@Service
public class ConversationSessionService {
    private Map<String, ConversationSession> sessions = new HashMap<>();
    
    public ConversationSession getOrCreateSession(String sessionId, String scenario, String userLevel) {
        if (!sessions.containsKey(sessionId)) {
            sessions.put(sessionId, new ConversationSession(sessionId, scenario, userLevel));
        }
        return sessions.get(sessionId);
    }
}