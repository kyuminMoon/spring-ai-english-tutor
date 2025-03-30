package com.tistory.kmmoon.chatgptapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 영어 시나리오 컨트롤러
 */
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {
    
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getScenarios() {
        List<Map<String, String>> scenarios = new ArrayList<>();
        
        // 카페에서 주문하기 시나리오
        Map<String, String> scenario1 = new HashMap<>();
        scenario1.put("id", "cafe-ordering");
        scenario1.put("title", "카페에서 주문하기");
        scenario1.put("description", "바리스타와 대화하며 커피나 음료를 주문하는 상황을 연습합니다.");
        scenarios.add(scenario1);
        
        // 공항에서 체크인 시나리오
        Map<String, String> scenario2 = new HashMap<>();
        scenario2.put("id", "airport-checkin");
        scenario2.put("title", "공항에서 체크인하기");
        scenario2.put("description", "항공사 직원과 대화하며 체크인 및 관련 질문을 처리합니다.");
        scenarios.add(scenario2);
        
        // 취업 인터뷰 시나리오
        Map<String, String> scenario3 = new HashMap<>();
        scenario3.put("id", "job-interview");
        scenario3.put("title", "취업 인터뷰");
        scenario3.put("description", "면접관과의 대화를 통해 취업 인터뷰 상황을 연습합니다.");
        scenarios.add(scenario3);
        
        return ResponseEntity.ok(scenarios);
    }
}
