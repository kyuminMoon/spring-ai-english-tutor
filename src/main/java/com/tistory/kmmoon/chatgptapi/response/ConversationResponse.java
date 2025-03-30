package com.tistory.kmmoon.chatgptapi.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ConversationResponse {
    private String response;
    private String feedback;
    private List<String> suggestions;
}