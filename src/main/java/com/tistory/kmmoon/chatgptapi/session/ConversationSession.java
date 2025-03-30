package com.tistory.kmmoon.chatgptapi.session;

import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 대화 상태를 유지하기 위한 세션 클래스
 */
@Getter
@Setter
public class ConversationSession {
    private String sessionId;
    private String scenario;
    private String userLevel;
    private List<Message> conversationHistory = new ArrayList<>();

    /**
     * 세션 생성자
     */
    public ConversationSession(String sessionId, String scenario, String userLevel) {
        this.sessionId = sessionId;
        this.scenario = scenario;
        this.userLevel = userLevel;

        // 시나리오와 레벨이 제공된 경우만 초기 시스템 메시지 추가
        if (!scenario.isEmpty() && !userLevel.isEmpty()) {
            String systemPrompt = createSystemPrompt(scenario, userLevel);
            conversationHistory.add(new SystemMessage(systemPrompt));
        }
    }

    /**
     * 사용자 메시지 추가
     */
    public void addUserMessage(String content) {
        conversationHistory.add(new UserMessage(content));
    }

    /**
     * AI 응답 추가
     */
    public void addAssistantMessage(String content) {
        conversationHistory.add(new AssistantMessage(content));
    }

    /**
     * 시나리오 업데이트
     */
    public void updateScenario(String scenario, String userLevel) {
        this.scenario = scenario;
        this.userLevel = userLevel;

        // 기존 시스템 메시지 제거
        conversationHistory.removeIf(msg -> msg instanceof SystemMessage);

        // 새로운 시스템 메시지 추가
        String systemPrompt = createSystemPrompt(scenario, userLevel);
        conversationHistory.add(0, new SystemMessage(systemPrompt));
    }

    /**
     * 시스템 프롬프트 생성
     */
    private String createSystemPrompt(String scenario, String userLevel) {
        return String.format("""
            당신은 영어 교육 애플리케이션의 대화 파트너입니다.
            다음 시나리오에 맞게 영어로 대화를 진행하세요: %s
            
            사용자의 영어 수준은 %s입니다. 이 수준에 맞게 대화를 조정하세요.
            
            대화 규칙:
            1. 항상 영어로만 대화하세요.
            2. 사용자가 영어로 말하도록 유도하세요.
            3. 문법 오류가 있으면 자연스럽게 수정된 표현을 사용하세요.
            4. 대화는 현실적이고 자연스러워야 합니다.
            5. 응답은 반드시 다음 JSON 형식으로만 제공하세요(어떤 설명도 JSON 앞뒤에 붙이지 마세요):
            {
              "response": "당신의 영어 대화 응답(영어로만)",
              "feedback": "사용자 영어에 대한 피드백 (한국어로)",
              "suggestions": ["다음에 말할 수 있는 표현 1", "표현 2", "표현 3"]
            }
            
            사용자가 한국어로 질문하더라도 영어로 대화를 이끌어가되, feedback 부분에서만 한국어로 설명하세요.
            응답은 반드시 위의 JSON 형식으로만 제공하고, 다른 어떤 설명이나 추가 텍스트도 포함하지 마세요.
            """, scenario, userLevel);
    }
}