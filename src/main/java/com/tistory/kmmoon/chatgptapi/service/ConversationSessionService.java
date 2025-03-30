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

    /**
     * 세션을 가져오거나 새로 생성합니다.
     */
    public ConversationSession getOrCreateSession(String sessionId, String scenario, String userLevel) {
        if (!sessions.containsKey(sessionId)) {
            sessions.put(sessionId, new ConversationSession(sessionId, scenario, userLevel));
        } else if (!scenario.isEmpty() && !userLevel.isEmpty()) {
            // 기존 세션이 있지만 새로운 시나리오와 레벨이 지정된 경우 업데이트
            ConversationSession existingSession = sessions.get(sessionId);
            existingSession.updateScenario(scenario, userLevel);
        }
        return sessions.get(sessionId);
    }

    /**
     * 세션을 제거합니다.
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}