package com.tistory.kmmoon.chatgptapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tistory.kmmoon.chatgptapi.request.ConversationRequest;
import com.tistory.kmmoon.chatgptapi.response.ConversationResponse;
import com.tistory.kmmoon.chatgptapi.service.ConversationSessionService;
import com.tistory.kmmoon.chatgptapi.session.ConversationSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 영어 대화 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/conversation")
public class EnglishConversationController {

    private final ChatClient chatClient;

    private final ConversationSessionService sessionService;

    private static final int MAX_HISTORY_SIZE = 5;

    @PostMapping("/{sessionId}")
    public ResponseEntity<ConversationResponse> conversation(
            @PathVariable String sessionId,
            @RequestBody ConversationRequest request) {

        // 세션 가져오기 또는 생성
        ConversationSession session = sessionService.getOrCreateSession(
                sessionId, request.getScenario(), request.getUserLevel());

        // 사용자 메시지 추가
        session.addUserMessage(request.getUserMessage());

        // 프롬프트 생성 및 옵션 설정
        Prompt prompt = new Prompt(session.getConversationHistory());

        try {
            // ChatGPT API 호출
            ChatResponse response = chatClient.prompt(prompt)
                    .call()
                    .chatResponse();
            String responseContent = response.getResult().getOutput().getText();

            // JSON 응답 파싱
            ConversationResponse conversationResponse = parseJsonResponse(responseContent);

            // 성공적으로 파싱된 경우만 대화 기록에 추가
            // 파싱에 실패하고 기본 응답이 반환된 경우는 기록에 추가하지 않음
            if (responseContent.trim().startsWith("{") || responseContent.contains("{\"response\":")) {
                session.addAssistantMessage(responseContent);
            }

            return ResponseEntity.ok(conversationResponse);
        } catch (Exception e) {
            // API 호출 오류 처리
            ConversationResponse errorResponse = new ConversationResponse();
            errorResponse.setResponse("An error occurred while processing your request.");
            errorResponse.setFeedback("API 호출 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.setSuggestions(List.of("잠시 후 다시 시도해 주세요"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 대화 내역 크기를 제한합니다
     */
    private void trimConversationHistory(ConversationSession session) {
        List<Message> messages = session.getConversationHistory();

        // 시스템 메시지를 제외한 사용자 및 어시스턴트 메시지만 계산
        long nonSystemMessageCount = messages.stream()
                .filter(msg -> !(msg instanceof SystemMessage))
                .count();

        // 최대 크기를 초과하면 가장 오래된 메시지부터 제거
        if (nonSystemMessageCount > MAX_HISTORY_SIZE * 2) { // 사용자와 어시스턴트 메시지 쌍이므로 2배
            int toRemove = (int) (nonSystemMessageCount - MAX_HISTORY_SIZE * 2);

            // 시스템 메시지를 제외하고 가장 오래된 메시지부터 제거
            int removed = 0;
            for (int i = 0; i < messages.size() && removed < toRemove; i++) {
                if (!(messages.get(i) instanceof SystemMessage)) {
                    messages.remove(i);
                    i--; // 인덱스 조정
                    removed++;
                }
            }
        }
    }


    /**
     * 대화 내역을 조회합니다
     */
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<Message>> getConversationHistory(@PathVariable String sessionId) {
        ConversationSession session = sessionService.getOrCreateSession(sessionId, "", "");
        return ResponseEntity.ok(session.getConversationHistory());
    }

    /**
     * 대화 내역을 초기화합니다
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String sessionId) {
        // 기존 세션을 제거하고 새 세션 생성
        sessionService.removeSession(sessionId);
        return ResponseEntity.ok().build();
    }



    /**
     * ChatGPT API 응답을 파싱하여 ConversationResponse 객체로 변환합니다.
     * ChatGPT API는 구조화된 JSON을 반환할 수 있도록 프롬프트에서 요청해야 합니다.
     */
    private ConversationResponse parseJsonResponse(String responseContent) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // JSON 형식 확인 (응답이 중괄호로 시작하는지)
            if (responseContent.trim().startsWith("{")) {
                // 전체 텍스트를 JSON으로 파싱 시도
                return objectMapper.readValue(responseContent, ConversationResponse.class);
            } else {
                // JSON 형식이 아닌 경우, JSON 부분 추출 시도
                int jsonStart = responseContent.indexOf('{');
                int jsonEnd = responseContent.lastIndexOf('}') + 1;

                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonPart = responseContent.substring(jsonStart, jsonEnd);
                    try {
                        return objectMapper.readValue(jsonPart, ConversationResponse.class);
                    } catch (JsonProcessingException e) {
                        log.warn("JSON 일부 추출 파싱 실패: {}", e.getMessage());
                    }
                }

                // JSON 파싱 실패 시 기본 응답 생성
                log.warn("API 응답에서 JSON 형식을 찾을 수 없습니다: {}", responseContent);

                // 텍스트 응답을 기본 형식으로 변환
                ConversationResponse fallbackResponse = new ConversationResponse();
                fallbackResponse.setResponse(responseContent.trim());
                fallbackResponse.setFeedback("API가 구조화된 응답을 반환하지 않았습니다.");
                fallbackResponse.setSuggestions(List.of("다시 질문해 보세요", "다른 표현으로 시도해 보세요"));
                return fallbackResponse;
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());

            // 오류 발생 시 기본 응답
            ConversationResponse errorResponse = new ConversationResponse();
            errorResponse.setResponse("죄송합니다, 응답을 처리하는 중 오류가 발생했습니다.");
            errorResponse.setFeedback("API 응답 처리 중 오류: " + e.getMessage());
            errorResponse.setSuggestions(List.of("잠시 후 다시 시도해 주세요"));
            return errorResponse;
        }
    }
}