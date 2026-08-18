# AGENTS.md

## 1. Purpose

이 문서는 Routine Log 프로젝트에서 AI Coding Agent가 코드를 작성하거나 수정할 때 따라야 하는 개발 규칙을 정의한다.

Agent는 작업을 시작하기 전에 이 문서와 관련 설계 문서를 확인해야 한다.

관련 문서:

```text
docs/PROJECT_PLAN.md
docs/ARCHITECTURE.md
docs/ERD.md
docs/API.md
```

문서와 실제 코드가 충돌하는 경우 임의로 구조를 변경하지 말고 기존 구현과 요구사항을 먼저 확인한다.

---

# 2. Project Overview

Routine Log는 영상 인증 기반 루틴 기록 웹 서비스이다.

핵심 흐름:

```text
반복 루틴 생성
→ 날짜별 루틴 생성
→ 루틴 수행
→ 영상 인증
→ SUCCESS 처리
→ 하루 성공 영상 수집
→ Daily Vlog 생성
```

Repository는 Frontend와 Backend를 하나의 Git Repository에서 관리하는 Monorepo 구조를 사용한다.

```text
routine-log/
├─ backend/
├─ frontend/
├─ docs/
├─ infra/
├─ monitoring/
├─ nginx/
└─ AGENTS.md
```

---

# 3. Technology Stack

## Backend

```text
Java
Spring Boot
Gradle
Spring Security
JWT
Spring Data JPA
MySQL
Spring Boot Actuator
Micrometer
```

## Frontend

```text
React
TypeScript
Vite
Tailwind CSS
Pretendard
```

## Infrastructure

```text
AWS
Docker
Docker Compose
Nginx
Jenkins
Terraform
```

## Video

```text
AWS S3
FFmpeg
```

---

# 4. General Development Rules

모든 개발 작업에서 다음 원칙을 따른다.

1. 현재 요청한 범위만 수정한다.
2. 요청하지 않은 기능을 추가하지 않는다.
3. 기존 코드를 먼저 확인한 뒤 수정한다.
4. 기존 구조와 Naming Convention을 유지한다.
5. 불필요한 파일 이동이나 대규모 Refactoring을 하지 않는다.
6. 필요하지 않은 Dependency를 추가하지 않는다.
7. 같은 기능을 중복 구현하지 않는다.
8. 단순한 문제에 과도한 추상화를 적용하지 않는다.
9. MVP 구현을 우선한다.
10. 향후 확장을 이유로 현재 필요하지 않은 구조를 미리 만들지 않는다.
11. 기존 기능이 동작하는 상태를 유지한다.
12. 관련 없는 Formatting 변경을 최소화한다.

---

# 5. Before Editing Code

코드를 수정하기 전에 반드시 다음 순서로 확인한다.

```text
1. 요청 내용 확인
2. 관련 파일 확인
3. 기존 구현 확인
4. 관련 문서 확인
5. 영향 범위 확인
6. 필요한 파일만 수정
7. 테스트 또는 빌드 수행
```

파일 내용을 확인하지 않은 상태에서 기존 구현을 추측하여 수정하지 않는다.

새로운 Class나 Component를 만들기 전에 동일한 역할의 코드가 이미 존재하는지 확인한다.

---

# 6. Scope Control

Agent는 사용자의 요청 범위를 임의로 확장해서는 안 된다.

예:

사용자 요청:

```text
로그인 API 구현
```

허용:

```text
LoginRequest
LoginResponse
AuthController
AuthService
JWT 발급에 필요한 코드
관련 테스트
```

허용하지 않음:

```text
Refresh Token 전체 시스템 추가
OAuth 로그인 추가
Google 로그인 추가
Redis Session 추가
회원 탈퇴 기능 추가
```

추가 기능이 필요하다고 판단되는 경우에도 현재 작업에 반드시 필요한 경우에만 구현한다.

---

# 7. Backend Package Structure

Backend 기본 Package 구조는 다음을 따른다.

```text
com.routinelog
├─ auth
├─ user
├─ routine
├─ dailyroutine
├─ video
├─ vlog
└─ common
```

각 Domain은 가능한 한 기능 중심으로 구성한다.

예:

```text
routine/
├─ controller/
├─ service/
├─ repository/
├─ domain/
└─ dto/
```

프로젝트 규모에 비해 불필요하게 세분화하지 않는다.

---

# 8. Backend Layer Responsibilities

## Controller

Controller의 역할:

* HTTP Request 수신
* Request DTO 검증
* 인증 사용자 정보 전달
* Service 호출
* HTTP Response 반환

Controller에서 비즈니스 로직을 구현하지 않는다.

금지 예:

```java
@PostMapping
public ResponseEntity<?> create(...) {
    // Entity 직접 생성
    // Repository 직접 호출
    // 복잡한 상태 변경
}
```

---

## Service

Service는 비즈니스 로직을 담당한다.

역할:

* Entity 조회
* 권한 확인
* 상태 변경
* Domain 규칙 처리
* Repository 호출
* Transaction 관리

---

## Repository

Repository는 데이터 접근만 담당한다.

Repository에 비즈니스 로직을 넣지 않는다.

---

## Entity

Entity는 Database와 Domain 상태를 표현한다.

가능하면 상태 변경을 의미 있는 Method로 표현한다.

예:

```java
dailyRoutine.succeed();
dailyRoutine.fail(reason);
```

다음과 같이 Service에서 Setter를 반복 호출하는 방식은 피한다.

```java
dailyRoutine.setStatus(...);
dailyRoutine.setFailureReason(...);
```

---

# 9. DTO Rules

Controller API에서는 Entity를 직접 Request 또는 Response로 사용하지 않는다.

반드시 DTO를 사용한다.

예:

```text
CreateRoutineRequest
UpdateRoutineRequest
RoutineResponse
LoginRequest
LoginResponse
```

Entity를 그대로 반환하지 않는다.

금지:

```java
public User getUser()
```

권장:

```java
public UserResponse getUser()
```

---

# 10. Java Naming Convention

Java 기본 Naming Convention을 따른다.

## Class

PascalCase

```text
RoutineService
DailyRoutineController
UserRepository
```

## Method / Variable

camelCase

```text
createRoutine()
dailyRoutine
scheduledTime
```

## Constant

UPPER_SNAKE_CASE

```text
MAX_VIDEO_DURATION
ACCESS_TOKEN_EXPIRE_TIME
```

## Enum

Type:

```text
RoutineStatus
DayOfWeek
VlogStatus
```

Value:

```text
PENDING
SUCCESS
FAILED
```

---

# 11. Database Rules

Database 구조는 `docs/ERD.md`를 기준으로 한다.

기본 원칙:

1. Primary Key는 `id`를 사용한다.
2. Foreign Key 관계를 명확하게 정의한다.
3. 필요한 Unique Constraint를 Database 수준에서도 적용한다.
4. 상태값은 문자열 Enum 사용을 우선한다.
5. 시간 데이터 타입을 일관되게 사용한다.
6. Entity 관계에 무분별하게 Cascade를 적용하지 않는다.
7. Fetch 전략을 명시적으로 고려한다.
8. N+1 문제가 발생할 가능성이 있는 조회는 확인한다.

Enum은 기본적으로 다음 형식을 우선한다.

```java
@Enumerated(EnumType.STRING)
```

ORDINAL 방식은 사용하지 않는다.

---

# 12. BaseEntity

공통 생성/수정 시간이 필요한 Entity는 `BaseEntity`를 상속한다.

예:

```text
createdAt
updatedAt
```

공통 Entity 필드를 여러 Entity에 반복해서 작성하지 않는다.

단, 실제로 모든 Entity에서 필요한 필드만 BaseEntity에 넣는다.

---

# 13. Exception Handling

Backend 예외는 공통 Exception Handler를 통해 처리한다.

Controller마다 반복적인 try/catch를 작성하지 않는다.

권장 구조:

```text
common/
└─ exception/
   ├─ GlobalExceptionHandler
   ├─ ErrorCode
   └─ BusinessException
```

API Error Response는 프로젝트에서 정의한 공통 형식을 유지한다.

예:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ROUTINE_NOT_FOUND",
    "message": "루틴을 찾을 수 없습니다."
  }
}
```

---

# 14. API Rules

API 명세는 `docs/API.md`를 기준으로 한다.

Base Path:

```text
/api/v1
```

HTTP Method는 의미에 맞게 사용한다.

```text
GET     조회
POST    생성
PATCH   일부 변경
PUT     전체 변경이 필요한 경우
DELETE  삭제
```

API URL에는 가능한 한 동사보다 Resource 중심 Naming을 사용한다.

권장:

```text
GET /api/v1/routines
POST /api/v1/routines
DELETE /api/v1/routines/{routineId}
```

불필요한 예:

```text
/getRoutines
/createRoutine
/deleteRoutine
```

---

# 15. HTTP Status Rules

기본적으로 다음 상태 코드를 사용한다.

```text
200 OK
201 Created
204 No Content

400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict

500 Internal Server Error
```

모든 요청에 무조건 `200 OK`를 반환하지 않는다.

---

# 16. Authentication Rules

인증은 Spring Security + JWT를 사용한다.

기본 원칙:

1. 비밀번호는 평문으로 저장하지 않는다.
2. PasswordEncoder를 이용하여 비밀번호를 암호화한다.
3. JWT Secret은 코드에 하드코딩하지 않는다.
4. 인증이 필요한 API는 Security 설정에서 보호한다.
5. 사용자 ID를 Request Body에서 신뢰하지 않는다.
6. 로그인된 사용자 정보를 인증 Context에서 가져온다.

금지 예:

```json
{
  "userId": 1,
  "routineId": 10
}
```

사용자가 자신의 ID를 임의로 전달하여 권한을 결정하도록 설계하지 않는다.

---

# 17. Authorization Rules

사용자가 Resource를 수정하거나 삭제할 때 반드시 소유권을 확인한다.

예:

```text
User A가 User B의 Routine을 수정할 수 없어야 한다.
```

확인이 필요한 주요 Resource:

```text
Routine
DailyRoutine
Video
DailyVlog
```

ID를 알고 있다는 이유만으로 Resource 접근을 허용하지 않는다.

---

# 18. Security Rules

다음 정보는 Git Repository에 Commit하지 않는다.

```text
DB Password
JWT Secret
AWS Access Key
AWS Secret Key
Production Credential
Private Key
```

민감정보는 환경변수 또는 Secret 관리 방식을 사용한다.

예:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
S3_BUCKET_NAME
```

`.env.example`에는 실제 Secret을 넣지 않는다.

---

# 19. Transaction Rules

데이터 변경 작업은 적절한 Transaction 범위에서 수행한다.

예:

```java
@Transactional
public void updateRoutine(...) {
}
```

단순 조회는 필요한 경우:

```java
@Transactional(readOnly = true)
```

를 사용한다.

Controller에 `@Transactional`을 적용하지 않는다.

---

# 20. Routine Domain Rules

반복 루틴은 실제 날짜별 수행 기록과 구분한다.

```text
Routine
= 반복되는 루틴 Template

DailyRoutine
= 특정 날짜에 수행할 실제 루틴
```

예:

```text
Routine
매주 월~금 07:00 운동

↓

DailyRoutine

2026-08-17 07:00 운동
2026-08-18 07:00 운동
2026-08-19 07:00 운동
```

Routine 상태를 변경했다고 과거 DailyRoutine 기록을 임의로 변경하지 않는다.

---

# 21. DailyRoutine Rules

DailyRoutine 기본 상태:

```text
PENDING
SUCCESS
FAILED
```

기본 상태는 `PENDING`이다.

`FAILED` 상태에서는 실패 사유를 저장할 수 있다.

`SUCCESS` 처리는 영상 인증 흐름과 연결된다.

상태 변경 규칙은 한 위치에서 관리하고 여러 Controller에서 중복 구현하지 않는다.

---

# 22. Video Rules

영상은 Routine이 아닌 `DailyRoutine`에 연결한다.

기본 제약:

```text
최대 영상 길이: 15초
```

Frontend 검증만 신뢰하지 않는다.

Backend에서도 반드시 영상 제약을 검증한다.

S3에는 실제 파일을 저장하고 Database에는 Object Key 및 Metadata를 저장한다.

DB에 영상 Binary 자체를 저장하지 않는다.

---

# 23. S3 Rules

S3 Object Key는 일관된 규칙을 사용한다.

Object Key 규칙은 Architecture 문서에서 최종 확정한다.

임의로 다음과 같은 직접적인 Root Upload를 하지 않는다.

```text
video.mp4
test.mp4
abc.mp4
```

사용자와 날짜 또는 Resource 기준으로 충돌하지 않는 Object Key를 사용한다.

---

# 24. Daily Vlog Rules

Daily Vlog는 특정 날짜의 성공 영상으로 생성한다.

기본 처리 흐름:

```text
DailyRoutine 조회
→ SUCCESS 영상 조회
→ 순서 결정
→ FFmpeg 처리
→ S3 업로드
→ DailyVlog 저장
```

상태:

```text
PROCESSING
SUCCESS
FAILED
```

영상 처리 도중 실패할 수 있다는 것을 고려하여 상태를 관리한다.

---

# 25. Frontend Structure

Frontend 기본 구조:

```text
src/
├─ pages/
├─ features/
├─ components/
├─ api/
├─ hooks/
├─ types/
├─ assets/
├─ App.tsx
└─ main.tsx
```

---

# 26. Frontend Directory Responsibilities

## pages

Route 단위 화면.

예:

```text
HomePage
LoginPage
SignupPage
CalendarPage
RoutinePage
ProfilePage
```

---

## features

Domain별 기능 구현.

예:

```text
features/
├─ auth/
├─ routine/
├─ calendar/
├─ video/
└─ vlog/
```

---

## components

여러 화면에서 재사용 가능한 공통 UI.

예:

```text
Button
Card
Modal
BottomSheet
Navigation
```

특정 Page에서만 사용하는 Component를 무조건 공통 Components로 이동하지 않는다.

---

## api

Backend API 통신 코드.

Component에서 직접 반복적으로 `fetch`를 작성하지 않는다.

---

## hooks

재사용 가능한 React Hook.

재사용하지 않는 간단한 로직을 억지로 Custom Hook으로 만들지 않는다.

---

## types

공통 TypeScript Type 및 Interface.

---

# 27. React Component Rules

React Component는 가능한 한 한 가지 명확한 역할을 담당한다.

다음과 같은 지나치게 큰 Component가 되지 않도록 한다.

```text
HomePage.tsx
1000+ lines
```

다만 단순한 Component까지 지나치게 잘게 분리하지 않는다.

분리 기준:

* 반복 사용됨
* 독립적인 UI 역할이 있음
* 자체 상태 또는 로직이 있음
* Parent Component의 가독성을 크게 개선함

---

# 28. TypeScript Rules

가능한 한 명시적인 Type을 사용한다.

`any` 사용을 피한다.

금지:

```ts
const response: any = ...
```

Backend API Response 구조에 맞는 Type을 정의한다.

예:

```ts
interface Routine {
  id: number
  title: string
  scheduledTime: string
}
```

Type을 이유 없이 중복 정의하지 않는다.

---

# 29. Frontend API Rules

API 호출 로직은 한 곳에서 관리한다.

예:

```text
src/api/client.ts
src/features/auth/api.ts
src/features/routine/api.ts
```

JWT가 필요한 Request는 공통 API Client에서 Authorization Header 처리를 담당하도록 한다.

각 Component에서 Token Header 코드를 반복하지 않는다.

---

# 30. Frontend Authentication

JWT 저장 방식은 프로젝트에서 결정한 방식을 일관되게 사용한다.

인증 상태 처리는 한 곳에서 관리한다.

화면마다 개별적으로 로그인 여부를 판단하는 코드를 반복하지 않는다.

로그아웃 시 인증 정보를 제거하고 인증이 필요한 화면에 접근할 수 없도록 처리한다.

---

# 31. UI / UX Rules

Frontend 디자인은 다음 방향을 유지한다.

```text
Minimal
Premium
Clean
Modern
Lifestyle
Video Diary
```

Color:

```text
Black
Gray
White
```

기본 Font:

```text
Pretendard
```

UI 원칙:

* Mobile First
* max-width 480px
* 충분한 여백
* 충분한 Touch Area
* 일관된 Radius
* 얇은 Border
* Subtle Shadow
* 과도한 Animation 금지
* 화려한 Point Color 금지
* 이모지 장식 남발 금지
* 하나의 일관된 Icon Set 사용

특정 Apple Application UI를 그대로 복제하지 않는다.

---

# 32. Responsive Rules

서비스는 Mobile First로 개발한다.

기본 Container:

```css
max-width: 480px;
```

모바일 Browser에서 다음 요소를 고려한다.

```text
Safe Area
Bottom Navigation
Keyboard
Modal
Bottom Sheet
Camera Input
Video Player
```

Desktop에서도 사용 가능해야 하지만 모바일 UX를 우선한다.

---

# 33. Styling Rules

공통 Design Token을 우선 사용한다.

같은 의미의 값을 Component마다 임의로 다르게 작성하지 않는다.

예:

```text
spacing
radius
font size
font weight
border
shadow
```

Inline Style을 반복해서 작성하는 것보다 기존 Tailwind Utility와 공통 Component를 우선한다.

---

# 34. Dependency Rules

새 Dependency를 추가하기 전에 다음을 확인한다.

1. 현재 Dependency로 구현할 수 없는가?
2. 실제 프로젝트에서 반복적으로 필요한가?
3. 단순 기능 하나를 위해 과도한 Library를 추가하는 것은 아닌가?
4. 유지보수되고 있는 Library인가?

필요하지 않은 Dependency를 추가하지 않는다.

---

# 35. Testing Rules

새로운 핵심 기능을 구현할 때 관련 테스트를 함께 작성하거나 기존 테스트를 갱신한다.

우선적으로 테스트할 항목:

```text
Authentication
Authorization
Routine CRUD
DailyRoutine 생성
상태 변경
실패 사유
Video 연결
Daily Vlog 생성 상태
```

---

# 36. Backend Test Priority

테스트 우선순위:

```text
1. Domain / Service Test
2. Repository Test
3. Controller / API Integration Test
4. End-to-End Flow
```

단순 Getter/Setter를 위한 의미 없는 테스트를 작성하지 않는다.

---

# 37. Core Integration Test

Backend 핵심 통합 흐름:

```text
회원가입
→ 로그인
→ JWT 발급
→ 반복 루틴 생성
→ 날짜별 루틴 조회
→ DailyRoutine 생성
→ 상태 변경
```

영상 기능 추가 이후:

```text
DailyRoutine
→ Video Upload
→ SUCCESS
→ Daily Vlog
```

---

# 38. Build Verification

Backend 작업 후 가능한 경우 다음을 확인한다.

```bash
./gradlew test
./gradlew build
```

Frontend 작업 후 가능한 경우 다음을 확인한다.

```bash
npm run build
```

Lint Script가 구성되어 있다면 함께 확인한다.

```bash
npm run lint
```

Agent는 실행하지 않은 테스트를 실행했다고 말하지 않는다.

---

# 39. Error Fixing Rules

오류를 수정할 때 증상만 우회하지 않는다.

다음 순서로 확인한다.

```text
오류 메시지
→ 발생 파일
→ 직접 원인
→ 관련 구조
→ 최소 수정
→ 재검증
```

빌드를 통과시키기 위해 기능을 삭제하거나 검증을 무력화하지 않는다.

예:

```text
TypeScript 오류 → any로 전부 변경
Test 실패 → Test 삭제
Security 오류 → Security 전체 permitAll
```

위와 같은 방식은 사용하지 않는다.

---

# 40. Refactoring Rules

Refactoring은 다음 경우에 수행한다.

* 현재 기능 구현에 직접 필요함
* 중복 코드가 명확함
* 유지보수를 방해하는 구조가 있음
* 테스트 가능한 구조를 만들기 위해 필요함

기능 하나를 추가하면서 프로젝트 전체 구조를 동시에 재설계하지 않는다.

---

# 41. Git Rules

Commit에는 관련된 변경만 포함한다.

가능하면 Commit Message는 다음 Prefix를 사용한다.

```text
feat:
fix:
refactor:
test:
docs:
chore:
build:
ci:
```

예:

```text
feat: implement routine creation
fix: validate daily routine ownership
docs: update routine API specification
chore: configure spring profiles
```

민감정보를 Commit하지 않는다.

---

# 42. Documentation Rules

구현 결과가 기존 문서와 달라지는 경우 관련 문서를 갱신한다.

예:

API 변경:

```text
docs/API.md
```

DB 구조 변경:

```text
docs/ERD.md
```

Architecture 변경:

```text
docs/ARCHITECTURE.md
```

프로젝트 범위 변경:

```text
docs/PROJECT_PLAN.md
```

문서와 코드가 장기간 서로 다른 상태로 남지 않도록 한다.

---

# 43. Troubleshooting Documentation

개발 과정에서 의미 있는 문제와 해결 방법이 발생하면 향후 다음 문서에 기록한다.

```text
docs/TROUBLESHOOTING.md
```

기록할 가치가 있는 예:

* Spring Security 설정 문제
* JPA 관계 문제
* S3 권한 오류
* FFmpeg 영상 병합 실패
* Docker Network 문제
* Jenkins 배포 실패
* RDS 연결 실패
* Nginx Routing 문제

단순 오타는 기록하지 않는다.

---

# 44. Infrastructure Rules

Infrastructure는 서비스 구현과 함께 단계적으로 구축한다.

초기 MVP 단계에서 다음 구조를 먼저 사용한다.

```text
App EC2
├─ Nginx
├─ React
└─ Spring Boot

RDS
S3
```

별도 Management EC2에서:

```text
Jenkins
Prometheus
Grafana
```

필요성이 검증되기 전에 Kubernetes, EKS, Auto Scaling 등의 구조를 추가하지 않는다.

---

# 45. Terraform Rules

Terraform 도입 후 AWS Console에서 임의로 Infrastructure를 변경하는 것을 최소화한다.

기본 구조:

```text
infra/terraform/
├─ modules/
└─ environments/
```

Secret을 `.tf` 파일에 직접 작성하지 않는다.

Terraform State를 Git에 Commit하지 않는다.

금지:

```text
terraform.tfstate
terraform.tfstate.backup
```

---

# 46. Docker Rules

Container Image에는 민감정보를 포함하지 않는다.

환경에 따라 달라지는 값은 Runtime 환경변수로 전달한다.

Docker Image는 가능한 한 불필요한 파일을 포함하지 않는다.

`.dockerignore`를 사용한다.

Production 환경에서는 Development Server를 그대로 사용하지 않는다.

---

# 47. CI/CD Rules

CI/CD의 기본 흐름은 다음을 목표로 한다.

```text
GitHub Push
→ Jenkins
→ Test
→ Build
→ Docker Image
→ ECR Push
→ EC2 Deploy
→ Health Check
```

새 Container가 정상인지 확인하기 전에 기존 정상 Container를 제거하지 않는다.

배포 실패 시 이전 정상 버전을 유지할 수 있어야 한다.

---

# 48. Monitoring Rules

Application 상태는 Spring Boot Actuator를 통해 확인한다.

주요 관찰 대상:

```text
CPU
Memory
Disk
JVM
Container
API Response Time
Error Rate
HTTP 5xx
Database 상태
Application Health
```

모니터링 자체가 서비스 기능 개발보다 앞서지 않도록 한다.

---

# 49. Do Not Do

Agent는 명시적인 요구가 없는 한 다음 작업을 하지 않는다.

```text
OAuth 도입
Redis 도입
Kafka 도입
RabbitMQ 도입
Microservice 분리
Kubernetes 도입
EKS 도입
GraphQL 전환
Next.js 전환
Spring WebFlux 전환
NoSQL 추가
CQRS 적용
Event Sourcing 적용
```

기술적으로 가능하다는 이유만으로 추가하지 않는다.

---

# 50. Definition of Done

기능 작업은 다음 조건을 만족해야 완료된 것으로 본다.

```text
1. 요구 기능이 구현됨
2. 기존 기능이 깨지지 않음
3. 필요한 Validation이 존재함
4. 인증 / 권한이 필요한 경우 검증됨
5. Error 처리가 존재함
6. 관련 Test가 통과함
7. Build가 가능한 상태임
8. 필요하면 관련 문서가 갱신됨
```

---

# 51. Final Agent Principle

이 프로젝트에서 가장 중요한 원칙은 다음과 같다.

> 필요한 기능을 가장 단순하고 명확한 구조로 구현하고, 실제 요구가 생길 때 구조를 확장한다.

Agent는 기술적 복잡성을 프로젝트의 완성도로 착각하지 않는다.

우선순위는 항상 다음과 같다.

```text
Correctness
→ Security
→ Simplicity
→ Maintainability
→ Performance
→ Scalability
```

현재 단계에서 필요하지 않은 Scalability를 위해 Correctness와 Simplicity를 희생하지 않는다.
