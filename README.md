## MZ 세대를 위한 자산 관리 서비스 MoZi
<a href="https://github.com/user-attachments/assets/b2339118-6886-45f1-b0f3-7d3016e64087">
  <img src="https://github.com/user-attachments/assets/b2339118-6886-45f1-b0f3-7d3016e64087" width="800"/>
</a>

---

## 📌 프로젝트 개요

**Mozi(모지)**는 청년층 사용자의 퍼스널 정보(연령, 소득, 취업 상태 등)와 자산 목표를 기반으로  
**신청 가능한 정책 지원 정보와 금융 상품을 통합하여 추천**하는 맞춤형 자산관리 서비스입니다.

적합도 점수화 알고리즘을 통해 사용자의 상황과 목표에 최적화된 혜택을 직관적으로 탐색할 수 있으며,  
재무 목표 설정 → 달성률 확인 → 전략 제안 → 외부 자산관리 연계까지 **실질적인 자산관리 경험**을 제공합니다.

---

### 👥 담당

| 이름 | 역할 | GitHub | 담당 업무 |
|------|------|--------|------------|
| 장수영 | 부팀장 / 백엔드 개발 총괄 | [@jsy1021](https://github.com/jsy1021) | 금융 탐색 페이지(API) 구현, 스크랩 기능 개발 |


---
## 📅 개발 기간

- **전체 기간**: 2025년 7월 9일 ~ 2025년 8월 21일 (총 1.5개월)

### 🗓️ 주요 일정

| 기간 | 내용 |
|------|------|
| 1주차 (7.9 ~ 7.13) | 프로젝트 기획 및 설계 (요구사항 정의, 화면·ERD 설계) |
| 2주차 (7.14 ~ 7.29) | 백엔드 기본 기능 개발 (회원 관리, 인증/인가) |
| 3주차 (7.21 ~ 7.27) | 금융/정책 데이터 연동 (FSS API, 온통청년 API) |
| 4주차 (7.28 ~ 8.3) | 프론트엔드 UI/UX 개발 (목표 설정, 마이페이지) |
| 5차 (8.4 ~ 8.10) | 통합 테스트 및 개선 (프론트-백엔드 연동, UI 통일) |
| 6주차 (8.11 ~ 8.17) | 테스트, 버그 수정, UI 수정, 포트폴리오 작성 |
| 7주차 (8.18 ~ 8.21) | 프로젝트 마무리 (시연 영상, 포트폴리오) |
---

## 🛠️ 개발 환경 및 기술 스택

### 🖥 Frontend
- Vue.js 3 · Pinia · Vue Router  
- Axios · Bootstrap 5 · FontAwesome  

### ⚙️ Backend
- Java 17 · Spring MVC/Security  
- MyBatis · MySQL  
- JWT 인증 · OAuth2 (Kakao, Google)  
- FSS API · 공공데이터 API · 정책 스크래핑  

### 🔧 협업 및 관리 도구
- **GitHub**: 브랜치 전략 (개인 작업 브랜치 → 기능별 서브 브랜치 → main 병합),  
  Issues 기반 이슈 관리, Pull Request 코드 리뷰  
- **Notion**: 회의록, 일정 관리, 문서화  
- **[Figma](https://www.figma.com/design/LZQiTL0IbbKaBHh7Xh3CD7/PJT-17%EB%B0%98_2%ED%8C%80?node-id=2000-6141&t=VIX1biGMtnjZegnk-1)**: 와이어프레임, 프로토타입, UI/UX 협업  
- **Zoom / Zep / Slack**: 회의 및 실시간 커뮤니케이션

###

---

## 🎯 프로젝트 목표

- 🔍 **분산된 정책 및 금융 정보의 통합 제공**  
  → 여러 기관에 흩어진 정보를 사용자 중심으로 통합

- 🧠 **퍼스널 정보 기반 맞춤 추천 시스템 구축**  
  → 사용자의 조건과 목표에 맞는 신청 가능 혜택 자동 선별

- 📊 **정보의 시각화 및 전략 제공**  
  → 목표 달성률, 예상 달성 시점, 추천 전략 등을 실시간 제공

- 🤝 **행동으로 이어지는 사용자 경험 설계**  
  → 혜택 탐색에서 신청·가입까지의 흐름을 고려한 UX

- 📈 **청년층의 자산관리 루틴 정착**  
  → Mozi를 통해 자산관리를 생활 속 루틴으로 만듦

---

## ✨ 담당 기능

| 기능 구분 | 설명 |
|-----------|------|
| 💸 금융 상품 탐색 | 예금·적금 상품 조건 필터링, 금리 비교, 가입 은행별 정렬 |
| 🎯 목표 기반 추천 | 사용자 목표(예: 여행 자금, 전세자금 등)에 맞춘 금융상품 추천 |
| 📌 스크랩 기능 | 관심 있는 정책/금융 상품 즐겨찾기 |

<br/>

### ✨ 서비스 구현 기능

#### 추천 기능

<p align="left">
  <img src="https://github.com/user-attachments/assets/950ede46-29c0-4423-8f57-fec803f29ac5" height="420"/>
  <img src="https://github.com/user-attachments/assets/05523bde-599f-45cc-ad4f-7ac4dabc8939" height="420"/>
  <img src="https://github.com/user-attachments/assets/8ffc8bb4-1784-41a6-8d11-4cc82e691ffe" height="420"/>
</p>

* 목표가 설정되어있지 않은 경우 목표를 설정하도록 유도
* 사용자가 설정한 목표들에 대한 정책, 금융 상품에 대한 정보를 통합 조회 가능


#### 탐색 기능

<p align="left">
  <img src="https://github.com/user-attachments/assets/a2326dcc-6baa-42fa-80f2-30ba4522c721" height="420"/>
  <img src="https://github.com/user-attachments/assets/28908df5-5575-4738-ae10-6dcb4fbf6ee1" height="420"/>
  <img src="https://github.com/user-attachments/assets/ca39361b-91c1-46ab-ace8-e33cfdbd34b8" height="420"/>
</p>
<p align="left">
  <img src="https://github.com/user-attachments/assets/b664347d-220b-417b-ac30-beb3c32a5b4f" height="420"/>
  <img src="https://github.com/user-attachments/assets/2cb14161-27d9-4239-a153-1b5446eb24b6" height="420"/>
</p>

* 금융/정책 정보 탐색 기능 제공
* 조건별 필터(가입 방법·기간·은행) 및 금리 정렬 지원
* 퍼스널 정보 기반 정책 탐색 + 지역·연령·연소득 커스텀 탐색 가능
  
#### 스크랩 기능

<p align="left">
  <img src="https://github.com/user-attachments/assets/a643e3d0-8236-4332-bb3f-3de7e3f4d150" height="420"/>
  <img src="https://github.com/user-attachments/assets/91cd3c16-ab85-473e-8062-18183991b919" height="420"/>
</p>

* 금융, 정책 정보에 대한 스크랩 정보 저장, 삭제 구현


---
### 그 외 기능 

#### 로그인 및 회원가입
<p align="left">
  <img src="https://github.com/user-attachments/assets/d37ab61d-e4f2-4771-959b-29c2944d6523" height="420"/>
  <img src="https://github.com/user-attachments/assets/4c6ef79b-50c5-4734-877a-3d1cd24aa670" height="420"/>
  <img src="https://github.com/user-attachments/assets/0936fcad-f4fa-4643-871c-e56eaceb9a3e" height="417"/>
</p>

* 로그인: 일반 로그인 및 소셜 로그인 지원
* 회원가입: 이메일 인증 기반 사용자 확인

#### 퍼스널정보

<p align="left">
  <img src="https://github.com/user-attachments/assets/7261034c-f689-4c83-b127-71c5240c4a19" height="420"/>
  <img src="https://github.com/user-attachments/assets/35adda1a-d8b9-4e37-8bee-56eeacf48ee9" height="420"/>
  <img src="https://github.com/user-attachments/assets/08e02d2d-14b8-4f99-8837-27569ee2b71b" height="420"/>
</p>

<p align="left">
  <img src="https://github.com/user-attachments/assets/86cce23b-1104-4fbd-bd29-64d408a33d45" height="420"/>
  <img src="https://github.com/user-attachments/assets/45a0614d-5d9c-4e08-aff5-af6b31c8151f" height="420"/>
  <img src="https://github.com/user-attachments/assets/cd568d75-dcd6-4d55-b98b-386557831d07" height="420"/>
</p>

* 사용자 퍼스널 정보 입력 기능 제공
* 입력된 정보는 맞춤 정책 추천에 활용

#### 계좌 연동

<p align="left">
  <img src="https://github.com/user-attachments/assets/b33894b4-60b2-4449-8c33-9ee498e52f4a" height="420"/>
  <img src="https://github.com/user-attachments/assets/eaf6514a-4c1a-4cc3-9717-79cb14f0c191" height="420"/>
  <img src="https://github.com/user-attachments/assets/e7501cb2-fe88-4a27-b51e-af6a31fd90b1" height="420"/>
</p>

<p align="left">
  <img src="https://github.com/user-attachments/assets/bae015e9-54e3-46c3-97f6-371b5f02ba33" height="420"/>
  <img src="https://github.com/user-attachments/assets/bfec1daa-3cd7-40f7-ba51-47530734715c" height="420"/>
  <img src="https://github.com/user-attachments/assets/2831e4a3-ef8f-4c9d-9725-42b5b2cfb4af" height="420"/>
</p>

* 약관 동의 후 은행 ID 기반 계좌 연동 지원
* 연동 자산은 메인 및 자산 페이지에서 조회/관리 가능 (상세 조회, 주거래은행 설정, 연동 해지)


#### 목표 기능


<p align="left">
  <img src="https://github.com/user-attachments/assets/4102fda2-cfa7-48d4-870d-9a264e46776c" height="420"/>
  <img src="https://github.com/user-attachments/assets/cc8c778c-5311-4a35-9b26-5c55539b55ac" height="420"/>
  <img src="https://github.com/user-attachments/assets/a50c2d72-7146-46fa-ac53-743785126f41" height="420"/>
</p>
<p align="left">
  <img src="https://github.com/user-attachments/assets/79e1a58b-1383-4452-88a6-387bea8bc828" height="420"/>
  <img src="https://github.com/user-attachments/assets/91da1a54-dee7-480d-b977-6f3ba92b3f5e" height="420"/>
  <img src="https://github.com/user-attachments/assets/8f13db1e-bf28-47c9-bd81-8ef2fa72d1c6" height="420"/>
</p>

* 기본 목표(1억) 및 사용자 맞춤 목표 설정 가능 (목표명·금액·기간·계좌 연동 등)
* 설정된 목표와 연관된 정책 및 금융 상품 추천
* 목표 달성 시 주거래은행 자산 관리 센터로 연계


---
## ⚒️ 이슈 해결 및 트러블 슈팅

* 예·적금 상품 조회, 정책 조회 시 발생한 중복 쿼리를 캐싱 도입으로 최적화
* 예·적금 상품 조회 시 옵션 조회에서 발생한 N+1 문제 → 조인 기반 쿼리 + ResultMap 매핑으로 해결


---

## 📐 시스템 아키텍처


- **Frontend ↔ Backend ↔ API 연동** 구조  
- JWT 기반 인증 처리 및 OAuth2 로그인  
- 정기 API 동기화 및 정책 스크래핑 (Scheduler)  
- 목표 기반 추천 로직 및 적합도 점수화 알고리즘 구성  

<p align="center">
  <img src="https://github.com/user-attachments/assets/eb916d28-9929-4e01-bfd5-a8d70708fb28" width="750"/>
</p>

---
## 📁 프로젝트 폴더 구조


### 🖥 프론트엔드
```
📦
mozi-frontend
├─ public(이미지 저장 폴더)
│  └─ images
│     ├─ account
│     ├─ financial
│     ├─ goal
│     └─ recommend
├─ src
│  ├─ api(API 요청 스크립트 폴더)
│  ├─ App.vue
│  ├─ assets(정적 아이콘 저장 폴더)
│  │  └─ icons
│  │      ├─ bottom-nav
│  │      ├─ common
│  │      └─ top-nav
│  ├─ components(컴포넌트 관련 폴더)
│  │  ├─ goal
│  │  ├─ icons
│  │  ├─ layouts
│  │  └─ profile
│  ├─ constants(정적 데이터 관련 폴더)
│  ├─ main.js
│  ├─ pages(페이지 관련 폴더)
│  │  ├─ account
│  │  ├─ auth
│  │  ├─ goal
│  │  ├─ recommend
│  │  │  ├─ financial
│  │  │  ├─ policy
│  │  ├─ scrap
│  │  ├─ search
│  │  │  ├─ financialSearch
│  │  │  │  └─ util
│  │  │  ├─ policySearch
│  │  │  │  └─ util
│  │  └─ user
│  ├─ router(라우터 관리 폴더)
│  └─ stores(상태 관리 폴더)
└─ vite.config.js(빌드 도구 설정 파일)
```
<br/>

### ⚙️ 백엔드
```
📦
mozi-backend
├─ .github
├─ .gitignore
├─ build.gradle
├─ gradle
│  └─ wrapper
├─ gradlew
├─ gradlew.bat
├─ settings.gradle
└─ src
   ├─ main
   │  ├─ java
   │  │  └─ org
   │  │     └─ iebbuda
   │  │        └─ mozi
   │  │           ├─ common
   │  │           │  └─ response(API응답 명세 관련 폴더)
   │  │           ├─ config(설정 관련 폴더)
   │  │           ├─ domain(도메인 중심 설계+각 도메인은 계층별로 관리)
   │  │           │  ├─ account
   │  │           │  ├─ goal
   │  │           │  ├─ policy
   │  │           │  ├─ product
   │  │           │  ├─ profile
   │  │           |  |    ├─controller
   │  │           |  |    ├─domain
   │  │           |  |    ├─dto
   │  │           |  |    ├─mapper
   │  │           |  |    └─service
   │  │           │  ├─ recommend
   │  │           │  ├─ scrap
   │  │           │  ├─ security
   │  │           │  └─ user
   │  │           └─ exception(예외처리 관련 폴더)
   │  └─ resources
   │     ├─ log4j2.xml(로그 관련 설정 파일)
   │     ├─ mybatis-config.xml(MyBatis 설정 파일)
   │     └─ org
   │        └─ iebbuda
   │           └─ mozi
   │              ├─ domain
   │              │  ├─ account
   │              │  ├─ goal
   │              │  ├─ policy
   │              │  ├─ product
   │              │  ├─ profile
   │              │  ├─ recommend
   │              │  ├─ scrap
   │              │  ├─ security
   │              │  └─ user
   │              └─ mapper
   └─ test
      └─ java
         └─ org
            └─ iebbuda
               └─ mozi
                  ├─ config
                  ├─ domain
                  │  ├─ account
                  │  ├─ policy
                  │  ├─ profile
                  │  ├─ security
                  │  └─ user
                  └─ goal

```


### 🗄️ 데이터베이스 및 ERD
- ErdCloud 사용
- 사용자 정보, 목표 설정, 금융상품, 정책정보, 추천 결과 등으로 구성된 도메인 중심 설계
- 정규화된 테이블 구조로 효율적인 데이터 관리

<p align="center">
  <a href="https://github.com/user-attachments/assets/55576e86-2250-49d9-a08b-abeef39bed1b">
    <img src="https://github.com/user-attachments/assets/55576e86-2250-49d9-a08b-abeef39bed1b" width="800"/>
  </a>
</p>


---




