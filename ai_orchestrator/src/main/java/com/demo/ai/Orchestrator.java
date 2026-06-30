package com.demo.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public class Orchestrator {

    // 1-1. 초안 기획 에이전트 정의 (Lead Planner)
    interface LeadPlannerAgent {
        @SystemMessage("당신은 '리드 게임 기획자'입니다. 사용자의 요구사항을 바탕으로 뼈대가 되는 기본 게임 컨셉과 규칙 초안을 제안합니다. 한국어로 답변하세요.")
        String createDraftPlan(@UserMessage String userRequest);
    }

    // 1-2. 크리에이티브 디렉터 에이전트 정의 (Creative Director)
    interface CreativeDirectorAgent {
        @SystemMessage("당신은 '크리에이티브 디렉터(총괄 기획자)'입니다. 리드 기획자의 초안을 분석하고, 게임을 훨씬 더 재미있고 중독성 있게 만들 수 있는 독창적인 아이디어와 메커니즘을 덧붙여 최종 기획안을 확정합니다. 한국어로 답변하세요.")
        String finalizePlan(@UserMessage String draftPlan);
    }

    // 2. API 설계 에이전트 정의 (API Designer)
    interface ApiDesignerAgent {
        @SystemMessage("당신은 'API 설계자'입니다. 기획안을 바탕으로 프론트엔드 내부 데이터 상태 관리 구조나 백엔드 통신용 JSON API 명세서를 설계합니다. 한국어로 답변하세요.")
        String designApi(@UserMessage String plan);
    }

    // 3. 메인 코더 에이전트 정의 (Coder)
    interface CoderAgent {
        @SystemMessage("당신은 '리드 프로그래머'입니다. 기획안과 데이터 구조 설계를 바탕으로 React + Phaser 기반의 실제 게임 로직(핵심 코드)을 작성합니다. 코드 위주로 한국어로 답변하세요.")
        String writeCode(@UserMessage String instructions);
    }

    // 4. QA 테스터 에이전트 정의 (Tester)
    interface TesterAgent {
        @SystemMessage("당신은 'QA 엔지니어 및 테스터'입니다. 작성된 코드를 보고 예상되는 버그, 엣지 케이스 및 테스트 시나리오를 작성합니다. 한국어로 답변하세요.")
        String testCode(@UserMessage String code);
    }

    public static void main(String[] args) {
        System.out.println("🚀 AI Orchestrator 시작 (Ollama 연동)");

        // 1. 기획자 전용 모델 (예: 창의성이 뛰어난 가벼운 모델)
        ChatLanguageModel plannerModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2:1b")
                .temperature(0.8) // 창의성을 위해 조금 더 높임
                .timeout(java.time.Duration.ofMinutes(5))
                .build();

        // 2. 개발자(리뷰어) 전용 모델 (예: 코딩과 한국어에 강한 모델)
        ChatLanguageModel reviewerModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5:1.5b") // 리뷰어는 다른 모델을 지정!
                .temperature(0.3) // 정확성을 위해 온도를 낮춤
                .timeout(java.time.Duration.ofMinutes(5))
                .build();

        // 각 에이전트 생성 시 모델 객체를 주입 (기획 담당 2명은 plannerModel 사용)
        LeadPlannerAgent leadPlanner = AiServices.create(LeadPlannerAgent.class, plannerModel);
        CreativeDirectorAgent director = AiServices.create(CreativeDirectorAgent.class, plannerModel);
        ApiDesignerAgent apiDesigner = AiServices.create(ApiDesignerAgent.class, reviewerModel);
        CoderAgent coder = AiServices.create(CoderAgent.class, reviewerModel);
        TesterAgent tester = AiServices.create(TesterAgent.class, reviewerModel);

        String userRequest = "스마트폰에서 터치로 움직이는 단순한 '슬라임 피하기' 도트 게임";

        // Step 1-1: 기획 초안 작성
        System.out.println("\n[리드 기획자] 게임 기획 초안 작성 중...");
        String draftPlan;
        try {
            draftPlan = leadPlanner.createDraftPlan(userRequest);
            System.out.println("============== [초기 기획 초안] ==============\n" + draftPlan);
        } catch (Exception e) {
            System.err.println("❌ Ollama 연결 실패. 모델이 실행 중인지 확인하세요.");
            e.printStackTrace();
            return;
        }

        // Step 1-2: 기획 고도화 및 최종 확정
        System.out.println("\n[크리에이티브 디렉터] 기획안 아이디어 고도화 및 최종 확정 중...");
        String finalPlan = director.finalizePlan(draftPlan);
        System.out.println("============== [최종 기획안] ==============\n" + finalPlan);

        // Step 2: API 및 데이터 구조 설계
        System.out.println("\n[API 설계자] 데이터 구조 설계 중...");
        String apiSpec = apiDesigner.designApi(finalPlan);
        System.out.println("============== [API 및 데이터 구조 설계] ==============\n" + apiSpec);

        // Step 3: 핵심 코드 작성
        System.out.println("\n[메인 코더] 핵심 게임 코드 작성 중...");
        String promptForCoder = "기획안:\n" + finalPlan + "\n\n데이터구조:\n" + apiSpec;
        String code = coder.writeCode(promptForCoder);
        System.out.println("============== [게임 코드 작성] ==============\n" + code);

        // Step 4: QA 및 테스트 시나리오 작성
        System.out.println("\n[QA 테스터] 코드 리뷰 및 테스트 시나리오 작성 중...");
        String testPlan = tester.testCode(code);
        System.out.println("============== [테스트 시나리오] ==============\n" + testPlan);
        
        System.out.println("\n✅ 5단계 협업 워크플로우(Harness) 테스트 완료!");
    }
}
