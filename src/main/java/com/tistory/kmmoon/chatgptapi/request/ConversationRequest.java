package com.tistory.kmmoon.chatgptapi.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConversationRequest {
    private String userMessage;
    private String scenario;
    private String userLevel;
}