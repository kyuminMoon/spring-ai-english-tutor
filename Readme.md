# 영어 학습 대화 애플리케이션 (English Conversation Learning App)

Spring AI와 ChatGPT API 등을 활용한, 다양한 상황에서 영어 대화를 연습할 수 있는 토이 프로젝트입니다. 사용자의 영어 실력에 맞춰 자연스러운 대화를 제공하고, 문법 오류에 대한 피드백을 한국어로 제공합니다.

## 🚀 주요 기능

- **다양한 대화 시나리오**: 카페 주문, 공항 체크인, 취업 인터뷰 등 실생활에서 활용 가능한 영어 대화 시나리오 제공
- **수준별 대화 조정**: 초급, 중급, 고급 등 사용자의 영어 수준에 맞게 대화 난이도 조정
- **실시간 피드백**: 영어 표현에 대한 피드백과 개선 제안을 한국어로 제공
- **대화 흐름 유지**: 최근 5개의 대화 내역을 저장하여 자연스러운 대화 흐름 유지
- **다음 표현 제안**: 대화를 이어갈 수 있는 적절한 표현 3가지 제안

## 🛠️ 기술 스택

- **Backend**: Spring Boot, Spring AI
- **AI API**: Claude API / ChatGPT API
- **데이터 처리**: Jackson, Lombok
- **빌드 도구**: Gradle

## 💡 프로젝트 구현 과정에서의 고민점

### 1. AI API의 상태 비저장(Stateless) 특성 극복

ChatGPT와 Claude API는 기본적으로 이전 대화 내용을 기억하지 않는 상태 비저장(Stateless) 방식으로 동작합니다. 사용자와의 자연스러운 대화 흐름을 유지하기 위해 다음과 같은 방법을 고민했습니다:

- **대화 내역 관리 서비스 구현**: 세션별로 대화 내역을 저장하고 관리하는 서비스를 별도로 구현
- **Spring AI의 메시지 구조 활용**: 시스템 메시지, 사용자 메시지, 어시스턴트 메시지를 적절히 구성하여 컨텍스트 유지
- **서버 측 상태 관리**: 클라이언트가 아닌 서버에서 대화 내역을 관리하여 상태 유지

### 2. 토큰 소비 최적화

대화가 길어질수록 API 요청에 포함되는 토큰 수가 증가하여 응답 속도 저하 및 비용 증가 문제가 발생했습니다:

- **대화 내역 제한**: 최근 5개의 대화만 유지하여 토큰 소비 최적화 (실험 결과 5개가 컨텍스트 유지와 토큰 소비 사이의 최적점)
- **시스템 메시지 최적화**: 핵심 지시사항만 포함하여 시스템 메시지의 크기 최소화

### 3. 구조화된 응답 처리

AI의 응답을 일관된 형식으로 받기 위한 전략을 고민했습니다:

- **JSON 형식 강제**: 프롬프트에 명확한 JSON 응답 형식을 지정하여 구조화된 데이터 수신
- **강건한 파싱 로직**: AI가 지시를 완벽히 따르지 않는 경우를 대비한 다양한 예외 처리
- **피드백과 제안 분리**: 대화 응답, 피드백, 제안을 구분하여 UI에서 효과적으로 표현

### 4. 멀티턴 대화의 자연스러움 유지

사용자와 AI 간의 대화가 자연스럽게 이어지도록 하는 방법을 연구했습니다:

- **시나리오 기반 프롬프트**: 특정 상황과 역할을 명확히 설정하여 일관된 페르소나 유지
- **대화 컨텍스트 전달**: 이전 대화를 API 요청에 포함시켜 맥락 유지
- **사용자 수준 반영**: 사용자의 영어 수준에 맞게 응답 난이도 조정

## ⚙️ 버전 및 실행 방법

### 사용 버전

- JDK 23
- Gradle 8.13
- ChatGPT API 키

### 설치 단계

1. 환경 변수 설정
```yml
# application.yml 파일 수정
spring.ai.openai.api-key: {개인 API 키 추가}
```

2. 애플리케이션 빌드 및 실행
```bash
./gradlew bootRun
```

## 🌐 API 엔드포인트

### 대화 시작/계속
```
POST /api/conversation/{sessionId}
```
요청 본문:
```json
{
  "userMessage": "Hello, I want to order a coffee.",
  "scenario": "카페 주문하기",
  "userLevel": "intermediate"
}
```

### 대화 내역 조회
```
GET /api/conversation/{sessionId}/history
```

### 대화 내역 초기화
```
DELETE /api/conversation/{sessionId}
```

## 실제 GPT 호출 후 리턴되는 메시지

![img.png](img.png)

## 🔮 향후 개선 사항

- 데이터베이스 연동으로 대화 내역 영구 저장
- 사용자 인증 및 개인화 기능 추가
- 음성 인식 및 텍스트 음성 변환(TTS) 추가 (AWS 서비스 사용)
- 더 다양한 시나리오 추가
- 실시간 발음 평가 기능
- Claude API 등 타 API도 호출 가능하도록 설정 후 장애 시 서킷브레이커를 사용해 다른 API를 사용하도록 기능 추가
- 실무 적용에 있어선 HttpClient 관련 설정 튜닝과 로깅도 필요

---

이 프로젝트는 Spring AI의 실험적 기능과 ChatGPT API를 활용한 토이 프로젝트입니다. 실제 서비스 배포 시 API 사용량과 비용에 주의하세요.