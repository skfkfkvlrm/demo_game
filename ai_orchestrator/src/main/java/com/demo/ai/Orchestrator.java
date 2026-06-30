package com.demo.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public class Orchestrator {

    // 1. 기획 에이전트 정의 (Planner)
    interface PlannerAgent {
        @SystemMessage("당신은 '도트 게임 기획자'입니다. 사용자의 요구사항을 바탕으로 독창적이고 재미있는 게임 컨셉과 레벨 디자인을 제안합니다. 한국어로 답변하세요.")
        String planGame(@UserMessage String userRequest);
    }

    // 2. 기술 검토 에이전트 정의 (Code Reviewer)
    interface CodeReviewerAgent {
        @SystemMessage("당신은 '시니어 프론트엔드 개발자'입니다. 기획자의 기획안을 보고 기술적으로 React + Phaser 기반에서 구현 가능한지 검토하고, 코딩 가이드라인을 제시합니다. 한국어로 답변하세요.")
        String reviewPlan(@UserMessage String plan);
    }

    public static void main(String[] args) {
        System.out.println("🚀 AI Orchestrator 시작 (Ollama 연동)");

        // Ollama 모델 연결 설정
        // 주의: PC에 해당 모델(예: llama3, gemma 등)이 다운로드 되어 있어야 합니다. (명령어: ollama run llama3)
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma4:e4b") // 로컬에 설치된 모델명으로 변경 가능
                .temperature(0.7)
                .build();

        // 에이전트 객체 생성
        PlannerAgent planner = AiServices.create(PlannerAgent.class, model);
        CodeReviewerAgent reviewer = AiServices.create(CodeReviewerAgent.class, model);

        String userRequest = "스마트폰에서 터치로 움직이는 단순한 '슬라임 피하기' 도트 게임";

        // Step 1: 기획자가 기획안 작성
        System.out.println("\n[기획자 에이전트] 게임 기획 중...");
        String gamePlan;
        try {
            gamePlan = planner.planGame(userRequest);
            System.out.println("============== [초기 기획안] ==============\n" + gamePlan);
        } catch (Exception e) {
            System.err.println("❌ Ollama 연결 실패. 모델(gemma4:e4b)이 실행 중인지 확인하세요.");
            e.printStackTrace();
            return;
        }

        // Step 2: 리뷰어가 기술 검토
        System.out.println("\n[리뷰어 에이전트] 기획안 기술 검토 중...");
        String reviewResult = reviewer.reviewPlan(gamePlan);
        System.out.println("============== [기술 리뷰 결과] ==============\n" + reviewResult);
        
        System.out.println("\n✅ 협업 워크플로우(Harness) 테스트 완료!");
    }
}
