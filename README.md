# 🥗 SkinEat — Android (Frontend)

> 피부·식단 기록을 바탕으로 AI가 필요한 식재료를 추천하고, 장바구니까지 바로 연결해 주는 앱

멋쟁이사자처럼 아이디어톤 프로젝트 **SkinEat**의 안드로이드 앱(프론트엔드) 레포지토리입니다.
서버(백엔드) 등 나머지 파트는 아래 관련 레포를 참고해 주세요.

## 관련 링크

| 파트 | 링크 |
| --- | --- |
| Android (Frontend) | 현재 레포 |
| Backend | (https://github.com/Skin-Eat/SkinEat_back) |
| 완성 레포 | (https://github.com/Skin-Eat/SkinEat) |

## 문제 정의
피부 고민이 있어도 "어제 먹은 음식이 오늘 피부에 영향을 줬는지" 알기 어렵다는 점에서 출발했습니다.
식단 기록이 피부 변화로 이어진다는 걸 사용자가 직관적으로 느끼게 하는 것을 목표로 했습니다.

## 화면 구성

하단 탭은 **홈 · 리포트 · (기록 버튼) · 장바구니 · 마이**로 구성되어 있고, 가운데 기록 버튼에서 음식 기록과 피부 기록으로 이동합니다.

| 화면 | 설명 |
| --- | --- |
| 로그인 / 회원가입 | 계정 생성 및 로그인 |
| 온보딩 | 첫 실행 시 사용자 정보 입력 |
| 홈 | 오늘의 기록 요약 |
| 음식 기록 | 식단 입력, 음식 사진 AI 분석 |
| 피부 기록 | 피부 상태와 사진 기록 |
| 분석 / 리포트 | 피부 점수와 식습관 분석 |
| 장바구니 / 담은 재료 | 추천 식재료 확인 및 담기 |
| 레시피 상세 | 식재료별 조리법 |
| 마이 | 프로필, 로그아웃 |
| 히스토리 | 피부 사진 · 식습관 로그 전체 보기 |

## 스크린샷

<!-- 앱 화면 캡처를 추가해 주세요 -->
| 홈 | 기록 | 리포트 | 장바구니 |
| --- | --- | --- | --- |
| <img width="226" height="490" alt="image" src="https://github.com/user-attachments/assets/28f412ac-3679-4457-92bd-8f518b42e5da" /> | <img width="239" height="493" alt="image" src="https://github.com/user-attachments/assets/3513d5fb-37ec-4bf7-b246-9a02f08b8fff" /> | <img width="246" height="524" alt="image" src="https://github.com/user-attachments/assets/2352cbbb-9753-4b22-85a0-8ea7e15889d8" /> | <img width="255" height="529" alt="image" src="https://github.com/user-attachments/assets/a11aeaf1-9c94-464f-8bc3-dab07d03d298" /> |

## 디자인

기획과 UI 디자인을 직접 맡아 Figma로 전 화면을 설계한 뒤, Jetpack Compose로 구현했습니다.

### 핵심 디자인 결정
- **점수 + 한 줄 설명**: 피부 점수를 숫자로만 보여주지 않고, 어떤 음식이 영향을 줬는지 문장으로 함께 보여줘 기록의 의미를 바로 이해할 수 있게 했습니다.
- **의미 있는 컬러**: 피부에 좋은 음식은 그린, 주의가 필요한 음식은 레드로 구분하고, 색만으로 판단하지 않도록 경고 아이콘을 함께 사용했습니다.
- **가운데 기록 버튼**: 매일 반복되는 핵심 행동인 기록에 가장 빠르게 접근하도록 하단 탭 중앙에 배치했습니다.
- **기록 → 분석 → 추천 → 구매로 이어지는 흐름**: 분석 결과에서 부족한 성분을 알려주고, 바로 식재료 추천과 장바구니로 연결해 행동까지 이어지게 설계했습니다.

### 디자인 시스템
Figma 시안은 따뜻한 코랄 톤이었지만, 실제 기기에서 확인해 보니
'건강한 식단'이라는 서비스 성격에 비해 다소 무겁게 느껴졌습니다.
그래서 구현 단계에서 더 건강하고 시원한 인상을 주도록 컬러를 조정했습니다.
<img width="2400" height="1560" alt="image" src="https://github.com/user-attachments/assets/24470f30-c3b5-4c73-be11-5ed36bdcedf1" />

### Figma
[SkinEat 프로토타입 보기](https://www.figma.com/design/pUsX11gHs1GdHXolX9HqSl/SkinEat-%ED%94%84%EB%A1%9C%ED%86%A0%ED%83%80%EC%9E%85?node-id=0-1&t=uHlpSZqc4Kzzjt22-1)

## 주요 기능

- **식단 기록** — 먹은 음식을 기록하고, 음식 사진을 올리면 AI가 분석해 메뉴를 인식
- **피부 기록** — 그날의 피부 상태와 사진을 기록하고, 사진 히스토리로 변화를 확인
- **피부 점수 & 리포트** — 식단의 영양 성분을 바탕으로 피부 점수(혈당 부하·항염·장벽/수분·항산화)를 계산하고 리포트로 제공
- **AI 식재료 추천 & 장바구니** — 내 피부에 필요한 식재료를 추천받아 장바구니에 바로 담기
- **레시피** — 추천 식재료로 만들 수 있는 조리법 안내
- **기록 리마인더** — 매일 기록을 잊지 않도록 주기적인 알림
- **회원가입 / 로그인** — 로그인 세션 유지, 온보딩, 마이페이지

## 담당 역할

안드로이드 앱의 **전체 화면 UI 설계 및 구현**을 담당했습니다.

- 로그인부터 기록, 리포트, 장바구니, 마이페이지까지 전 화면 구현
- Jetpack Compose + Material 3 기반 공통 테마 구성

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 | Kotlin |
| UI | Jetpack Compose, Material 3 |
| 화면 이동 | Navigation Compose |
| 상태 관리 | ViewModel |
| 네트워크 | Retrofit, Gson, OkHttp |
| 로컬 저장 | DataStore (로그인 세션) |
| 백그라운드 작업 | WorkManager (리마인더 알림) |
| 빌드 | Gradle (Kotlin DSL), 버전 카탈로그 |
| SDK | minSdk 24 / targetSdk 34 |

## 프로젝트 구조

```
app/src/main/java/com/example/lionideaton/
└─com
    └─example
        └─lionideaton
            │  MainActivity.kt
            │  
            ├─data
            │  │  FoodSeedData.kt
            │  │  IngredientSeedData.kt
            │  │  RecipeSeedData.kt
            │  │
            │  ├─model
            │  │      Food.kt
            │  │      FoodImageAnalysisResponse.kt
            │  │      Ingredient.kt
            │  │      MealLog.kt
            │  │      Recipe.kt
            │  │      SkinLog.kt
            │  │      UserProfile.kt
            │  │
            │  ├─network
            │  │      NetworkModule.kt
            │  │      SkinBasketApi.kt
            │  │
            │  └─repository
            │          FoodAnalysisRepository.kt
            │
            ├─domain
            │      SkinScoreCalculator.kt
            │
            ├─navigation
            │      SkinBasketDestination.kt
            │
            ├─notification
            │      ReminderScheduler.kt
            │      ReminderWorker.kt
            │
            └─ui
                ├─screens
                │      AnalysisScreen.kt
                │      AuthScreen.kt
                │      BasketScreen.kt
                │      CartScreen.kt
                │      CartViewModel.kt
                │      FoodLogScreen.kt
                │      FoodLogViewModel.kt
                │      HomeScreen.kt
                │      MealLogHistoryScreen.kt
                │      MealLogViewModel.kt
                │      MyScreen.kt
                │      OnboardingScreen.kt
                │      RecipeScreen.kt
                │      ReportScreen.kt
                │      SkinLogScreen.kt
                │      SkinLogViewModel.kt
                │      SkinPhotoHistoryScreen.kt
                │      UserProfileViewModel.kt
                │
                └─theme
                        Color.kt
                        Theme.kt
                        Type.kt

```

## 시작하기

```bash
git clone https://github.com/Skin-Eat/SkinEat.git
```

Android Studio에서 클론한 폴더를 열고 Gradle 동기화가 끝나면 ▶️ **Run 'app'** 을 누릅니다.

## 팀원

| 이름 | 역할 | GitHub |
| --- | --- | --- |
| 이름 | Android (Frontend) | @heychaewong |
| 이름 | Backend | @youngwoofox-coder @gy8879 |
| 이름 | 기획 / 디자인 | @heychaewong @gy8879 |

