# agent 정의 가이드

JSON 파일에 '역할을 가진 에이전트(Agent)'를 정의하는 방법은, **그 JSON 파일을 읽고 해석하여 실제로 동작하는 시스템/엔진**이 무엇인지에 따라 구조가 완전히 달라집니다.

'에이전트'라는 용어가 최근 AI 분야에서 사용되면서, 일반적인 '프로세스 역할 분담' 외에도 크게 두 가지 맥락으로 이해할 수 있습니다.

---

### **🔍 Step 1: 에이전트의 종류 결정하기 (가장 중요)**

먼저 아래 질문에 답해 보세요. 어떤 시스템에게 이 JSON을 읽게 할 것인가요?

**A. [AI 오케스트레이션] - 역할 정의:**

- **목표:** 여러 개의 인공지능(LLM, 대규모 언어 모델)이 협업하여 복잡한 문제를 해결하는 시나리오를 구성할 때 사용합니다. (예: A 에이전트는 검색을 하고, B 에이전트는 분석 보고서를 작성하는 역할 분담)
- **적합 기술:** LangChain, AutoGen 등 AI 프레임워크

**B. [시스템/게임] - 프로세스 흐름 정의:**

- **목표:** 백엔드 시스템이나 게임 속 NPC(Non-Player Character), 또는 모듈 간의 데이터 처리 역할을 분담할 때 사용합니다. (예: 인증 에이전트, 결제 에이전트, 충돌 판정 에이전트)
- **적합 기술:** Workflow Engine (Apache Airflow 등), 게임 물리 엔진

---

### **📝 Step 2: 역할별 JSON 작성 가이드 및 예시**

어떤 목표를 가지고 계신지에 따라 필요한 구조가 다릅니다. 아래 두 가지 케이스 중 사용자님의 상황에 더 가까운 것을 참고하세요.

#### **🛠️ Case A: AI 에이전트 협업 시 (Role-Playing/LLM Workflow)**

이 구조는 각 에이전트의 **역할(Persona), 목표, 지식 범위**를 정의하는 데 초점을 맞춥니다.

```json
{
  "workflow_name": "도트게임 기획 피드백 루프",
  "description": "기획자가 작성한 원본 텍스트를 바탕으로 게임성과 기술적 구현 가능성을 동시에 평가하는 워크플로우.",
  "agents": [
    {
      "agent_id": "CreativeWriter",
      "role": "창의적인 기획자 (The Visionary)",
      "goal": "사용자의 흥미를 극대화할 수 있는 재미 요소를 제안하고, 스토리라인을 보강한다.",
      "persona": "긍정적이고 창의적이며 비현실적인 아이디어를 제시하는 작가 스타일로 응답해야 한다.",
      "input_needs": ["최초 기획서"],
      "output_format": "핵심 재미 요소 (3가지) 리스트 형태로 출력."
    },
    {
      "agent_id": "TechnicalPlanner",
      "role": "기술 검토자 (The Engineer)",
      "goal": "제안된 모든 기능이 현재 기술 스택(React, Spring Boot, Canvas 등)으로 구현 가능한지 판단하고 병목 지점을 경고한다.",
      "persona": "객관적이고 논리적이며, 항상 '구현성'을 최우선 순위로 고려해야 한다.",
      "input_needs": ["CreativeWriter의 결과물"],
      "output_format": "기술 리스크 보고서 (High/Medium/Low) 및 개선 코멘트 포함."
    },
    {
      "agent_id": "SystemOrchestrator",
      "role": "프로세스 관리자 (The Conductor)",
      "goal": "두 에이전트의 출력을 받아 최종적으로 '다음 단계에서 개발자가 집중해야 할 영역'을 요약하고 다음 액션 플랜(Action Plan)을 제시한다.",
      "input_needs": ["CreativeWriter 결과", "TechnicalPlanner 보고서"],
      "output_format": "최종 액션 아이템 목록 (번호 및 실행 주체 명시)."
    }
  ]
}
```

#### **🖥️ Case B: 시스템/게임 프로세스 역할 분담 시 (Workflow Engine)**

이 구조는 각 모듈이나 서비스가 어떤 **입력(Input)**을 받아 어떤 **출력(Output)**으로 상태를 변화시키는지에 초점을 맞춥니다.

```json
{
  "workflow_name": "플레이어 이동 및 충돌 판정 시스템",
  "description": "클라이언트의 입력 신호를 받아, 게임 규칙에 따라 위치를 확정하고 모든 참여자에게 전파하는 프로세스.",
  "agents": [
    {
      "agent_id": "InputHandler",
      "responsibility": "외부(WebSocket)로부터 Raw 데이터를 수신하여 유효성을 검증한다.",
      "trigger": "Client Input Event (X, Y)",
      "input_data_schema": { "timestamp": "number", "dx": "integer", "dy": "integer" },
      "processing_logic": "입력 좌표가 월드 경계(Boundary)를 벗어나는지 확인한다. 유효하지 않으면 무시하고 다음 단계로 전달.",
      "output": { "player_id": "number", "new_coords": ["x", "y"] }
    },
    {
      "agent_id": "PhysicsEngine",
      "responsibility": "입력된 좌표와 현재 맵의 지형 정보를 비교하여, 충돌 판정을 수행한다.",
      "trigger": "InputHandler.output (valid coords)",
      "input_data_schema": { "player_id": "number", "coords": ["x", "y"] },
      "processing_logic": "지형 데이터와 new_coords를 비교하여 충돌 여부를 판단한다. 막히면 원래 좌표로 되돌린다.",
      "output": { "collision_status": "boolean", "final_coords": ["x", "y"] }
    },
    {
      "agent_id": "BroadcasterService",
      "responsibility": "최종적으로 결정된 플레이어의 위치 정보를 모든 연결된 클라이언트에게 실시간으로 전송한다.",
      "trigger": "PhysicsEngine.output (final coords)",
      "input_data_schema": { "player_id": "number", "coords": ["x", "y"] },
      "processing_logic": "WebSocket을 통해 '좌표 업데이트' 메시지를 브로드캐스트한다.",
      "output": "실시간 상태 변화 (Broadcast Message)"
    }
  ]
}
```

---

### **🌟 최종 정리 및 요약: 무엇을 해야 할까요?**

| **상황** | **어떤 구조를 써야 하나요?** | **핵심 중점 요소** |
| --- | --- | --- |
| **AI와의 협업이 목표** (기획 검증, 보고서 작성 등) | 🤖 Case A (AI Agent Workflow) | `role`, `persona`, `goal`에 집중하여 각 에이전트의 '정체성'을 명확히 하세요. |
| **실제 시스템/게임 로직 구현이 목표** (좌표 이동, 결제 처리 등) | 🖥️ Case B (System Workflow) | `trigger` (발동 시점), `input_data_schema`, `processing_logic`에 집중하여 데이터 흐름을 정의하세요. |

JSON 파일 자체는 데이터를 담는 그릇일 뿐입니다. **가장 중요한 것은 이 JSON 파일을 읽고 다음 단계를 실행할 '엔진'을 설계하는 것**임을 명심해 주세요!