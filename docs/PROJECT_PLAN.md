# Routine Log — Project Plan

## 1. 프로젝트 개요

**Routine Log**는 사용자가 자신의 일상 루틴을 관리하고, 실제 수행 과정을 영상으로 인증하여 하루의 기록을 남길 수 있는 웹 서비스이다.

기존 루틴 관리 서비스가 단순히 체크 버튼을 눌러 완료 여부를 기록하는 방식에 집중한다면, Routine Log는 **영상 인증 기반의 루틴 기록**을 핵심 기능으로 한다.

사용자는 반복 루틴을 등록하고 매일 생성되는 루틴을 수행한 뒤 영상을 촬영하여 성공 여부를 기록할 수 있다.

하루 동안 성공한 루틴의 영상은 시간순으로 모아 하나의 **Daily Vlog**로 생성한다.

---

## 2. 프로젝트 목적

이 프로젝트의 목적은 다음과 같다.

### 서비스 목표

* 반복되는 일상 루틴을 쉽게 관리할 수 있도록 한다.
* 단순 체크가 아닌 영상 기반의 루틴 인증 기능을 제공한다.
* 성공 및 실패 기록을 날짜별로 확인할 수 있도록 한다.
* 하루 동안 수행한 루틴 영상을 하나의 Daily Vlog로 만든다.
* 루틴 수행 기록을 장기적으로 확인할 수 있도록 한다.

### 기술적 목표

* React와 Spring Boot 기반의 Frontend / Backend 분리 구조를 설계한다.
* JWT 기반 인증 및 인가 구조를 구현한다.
* JPA와 MySQL을 이용하여 관계형 데이터 구조를 설계한다.
* AWS S3를 활용하여 영상 파일을 관리한다.
* FFmpeg를 이용하여 여러 영상을 하나의 영상으로 병합한다.
* Docker 기반 애플리케이션 배포 환경을 구성한다.
* Jenkins를 이용한 CI/CD Pipeline을 구축한다.
* Prometheus / Grafana / CloudWatch 기반 모니터링 환경을 구축한다.
* Terraform을 이용하여 AWS Infrastructure as Code 환경을 구성한다.

---

## 3. 핵심 컨셉

Routine Log의 핵심 흐름은 다음과 같다.

```text
루틴 생성
    ↓
오늘의 루틴 생성
    ↓
루틴 수행
    ↓
영상 촬영
    ↓
영상 업로드
    ↓
루틴 성공 처리
    ↓
하루 성공 영상 수집
    ↓
Daily Vlog 생성
    ↓
캘린더에서 과거 기록 확인
```

핵심 가치는 다음 세 가지이다.

### 1. Routine

사용자가 반복적으로 수행해야 하는 행동을 등록하고 관리한다.

### 2. Proof

루틴 성공 여부를 단순 체크가 아닌 영상으로 기록한다.

### 3. Record

하루의 성공 기록을 영상으로 모아 Daily Vlog 형태로 보관한다.

---

## 4. Target User

다음과 같은 사용자를 주요 대상으로 한다.

* 매일 반복되는 루틴을 관리하고 싶은 사용자
* 단순 체크보다 실제 수행 기록을 남기고 싶은 사용자
* 운동, 공부, 생활 습관 등을 꾸준히 관리하고 싶은 사용자
* 자신의 하루 기록을 영상으로 남기고 싶은 사용자

---

## 5. 핵심 사용자 흐름

### 최초 사용자

```text
서비스 접속
→ 시작하기
→ 회원가입
→ 로그인
→ 반복 루틴 등록
→ 홈 화면 이동
```

### 일일 루틴 수행

```text
로그인
→ 오늘의 루틴 확인
→ 루틴 수행
→ 영상 촬영 또는 선택
→ 영상 업로드
→ SUCCESS 처리
```

루틴을 수행하지 못한 경우:

```text
루틴 선택
→ FAILED 처리
→ 실패 사유 입력
```

### Daily Vlog

```text
하루 루틴 수행
→ SUCCESS 상태의 영상 조회
→ 루틴 시간순 정렬
→ FFmpeg 영상 병합
→ Daily Vlog 생성
→ S3 저장
→ Daily Vlog 재생
```

### 과거 기록

```text
캘린더
→ 날짜 선택
→ 해당 날짜 루틴 기록 확인
→ SUCCESS / FAILED 확인
→ 영상 또는 Daily Vlog 확인
```

---

# 6. MVP 범위

초기 개발에서는 서비스 핵심 기능이 실제로 동작하는 것을 우선한다.

## 6.1 인증

구현 범위:

* 회원가입
* 로그인
* 로그아웃
* JWT 기반 인증
* 현재 로그인 사용자 조회

회원가입 기본 정보:

* 이메일
* 비밀번호
* 이름

---

## 6.2 반복 루틴

사용자는 반복적으로 수행할 루틴을 등록할 수 있다.

구현 범위:

* 반복 루틴 추가
* 반복 루틴 조회
* 반복 루틴 수정
* 반복 루틴 삭제
* 반복 요일 지정
* 루틴 수행 시간 지정
* 루틴 활성 / 비활성 관리

예:

```text
07:00 기상

반복:
월 / 화 / 수 / 목 / 금
```

---

## 6.3 날짜별 루틴

반복 루틴을 기반으로 특정 날짜의 실제 수행 기록을 관리한다.

구현 범위:

* 특정 날짜 루틴 조회
* 반복 루틴 기반 DailyRoutine 생성
* 오늘만 사용할 루틴 추가
* 날짜별 루틴 수정
* 날짜별 루틴 삭제
* 루틴 시간순 정렬

상태:

```text
PENDING
SUCCESS
FAILED
```

상태 의미:

* `PENDING`: 아직 수행하지 않은 루틴
* `SUCCESS`: 성공한 루틴
* `FAILED`: 수행하지 못한 루틴

FAILED 상태에서는 실패 사유를 저장할 수 있다.

---

## 6.4 영상 인증

루틴 수행 시 영상으로 인증할 수 있다.

구현 범위:

* 모바일 카메라 촬영
* 기존 영상 선택
* 최대 15초 제한
* 영상 파일 Backend 전송
* AWS S3 업로드
* 영상 메타데이터 DB 저장

영상 업로드 성공 시 해당 DailyRoutine 상태를 `SUCCESS`로 변경한다.

영상과 DailyRoutine은 연결되어 관리한다.

---

## 6.5 Daily Vlog

하루 동안 성공한 루틴 영상을 하나의 영상으로 만든다.

구현 범위:

* 특정 날짜 SUCCESS 영상 조회
* 루틴 수행 시간순 정렬
* FFmpeg를 이용한 영상 병합
* 생성된 영상 S3 업로드
* DailyVlog 메타데이터 저장
* 생성 상태 관리
* 생성된 영상 재생

DailyVlog 상태:

```text
PROCESSING
SUCCESS
FAILED
```

---

## 6.6 캘린더

사용자가 과거 루틴 기록을 확인할 수 있도록 한다.

구현 범위:

* 월간 캘린더
* 날짜 선택
* 해당 날짜 루틴 목록 조회
* SUCCESS / FAILED 상태 확인
* 실패 사유 확인
* Daily Vlog 확인

---

## 6.7 프로필

구현 범위:

* 사용자 이름 조회
* 사용자 이름 수정
* 프로필 이미지 조회
* 프로필 이미지 수정

---

# 7. Frontend 화면 구성

초기 화면은 다음과 같이 구성한다.

## 7.1 시작 화면

구성:

* Routine Log 로고
* 간단한 서비스 소개
* 시작하기 버튼

---

## 7.2 로그인

구성:

* 이메일
* 비밀번호
* 로그인
* 회원가입 이동

---

## 7.3 회원가입

구성:

* 이름
* 이메일
* 비밀번호
* 회원가입

---

## 7.4 Home

핵심 화면이다.

구성:

* 오늘 날짜
* 주간 캘린더
* 오늘의 루틴
* 루틴 시간
* 루틴 내용
* 루틴 상태
* 루틴 메뉴
* 루틴 추가
* 성공 / 실패 처리
* 오늘의 Daily Vlog

---

## 7.5 Calendar

구성:

* 월간 캘린더
* 날짜 선택
* 해당 날짜 루틴 기록
* 성공 / 실패 여부
* 실패 사유
* Daily Vlog

---

## 7.6 Routine

반복 루틴 관리 화면.

구성:

* 반복 루틴 목록
* 루틴 추가
* 반복 요일 선택
* 수행 시간 설정
* 루틴 수정
* 루틴 삭제

---

## 7.7 Profile

구성:

* 이름
* 프로필 이미지
* 사용자 정보 수정
* 로그아웃

---

# 8. UI / UX 원칙

Routine Log는 모바일 사용을 우선한다.

## 기본 방향

* Minimal
* Premium
* Clean
* Modern
* Lifestyle
* Video Diary

특정 애플리케이션을 그대로 복제하지 않고 미니멀하고 정돈된 디자인 원칙만 참고한다.

## Color

기본적으로 monochrome palette를 사용한다.

```text
Black
Gray
White
```

화려한 포인트 컬러는 사용하지 않는다.

## Typography

기본 폰트:

```text
Pretendard
```

제목, 본문, 보조 텍스트의 크기와 weight를 명확하게 구분한다.

## Layout

모바일 우선으로 설계한다.

기본 Content Width:

```text
max-width: 480px
```

고려 사항:

* Safe Area
* Bottom Navigation
* 충분한 터치 영역
* 한 손 조작
* 충분한 여백

## Component

UI는 다음 기준을 유지한다.

* 일관된 Button
* 일관된 Card
* 적절한 Radius
* 얇은 Border
* Subtle Shadow
* 최소한의 Animation
* 부드러운 상태 Transition

아이콘은 하나의 일관된 Icon Set을 사용한다.

이모지는 UI 장식 용도로 남발하지 않는다.

---

# 9. 기술 스택

## Frontend

```text
React
TypeScript
Vite
Tailwind CSS
Pretendard
```

---

## Backend

```text
Java
Spring Boot
Spring Security
JWT
Spring Data JPA
Gradle
Spring Boot Actuator
```

---

## Database

```text
MySQL
AWS RDS
```

---

## Video

```text
FFmpeg
AWS S3
```

---

## Infrastructure

```text
AWS VPC
AWS EC2
AWS RDS
AWS S3
AWS ECR
AWS IAM
AWS Route 53
AWS ACM
```

---

## Deployment

```text
Docker
Docker Compose
Nginx
Jenkins
```

---

## Monitoring

```text
Spring Boot Actuator
Micrometer
Prometheus
Grafana
Node Exporter
cAdvisor
CloudWatch
```

---

## Infrastructure as Code

```text
Terraform
```

---

# 10. 초기 배포 구조

초기에는 복잡한 Scale-out 구조보다 단순하고 관리 가능한 구조를 먼저 구축한다.

```text
Internet
    ↓
Route 53
    ↓
HTTPS
    ↓
App EC2
├─ Nginx
├─ React
└─ Spring Boot
       │
       ├─ RDS MySQL
       └─ S3

Management EC2
├─ Jenkins
├─ Prometheus
└─ Grafana
```

초기 App Server는 EC2 한 대를 기준으로 한다.

필요성이 확인되면 이후 ALB와 Scale-out 구조를 검토한다.

---

# 11. MVP 제외 기능

초기 버전에서는 다음 기능을 구현하지 않는다.

## Social

* 친구
* 팔로우
* 팔로워
* 좋아요
* 댓글
* 공유
* 실시간 채팅

## Advanced Video

* AI 영상 생성
* 자동 영상 편집
* 자동 자막
* 영상 필터
* BGM 자동 생성
* 영상 추천

## Infrastructure

초기 MVP에서는 다음 구조를 사용하지 않는다.

* Kubernetes
* EKS
* Argo CD
* GitOps
* Multi Region
* 다중 App Server
* 복잡한 Auto Scaling

이 기능들은 핵심 서비스 완성 이후 확장 단계에서 검토한다.

---

# 12. 개발 우선순위

## Phase 1 — Project Design (완료)

1. PROJECT_PLAN 작성
2. AGENTS 작성
3. ARCHITECTURE 작성
4. ERD 작성
5. API 명세 작성


---

## Phase 2 — Backend Foundation

1. Spring Boot 프로젝트 생성
2. Package 구조 생성
3. 환경설정
4. DB 연결
5. Entity 구현
6. Repository 구현
7. 공통 Exception 구조 구현

---

## Phase 3 — Authentication

1. Spring Security
2. JWT
3. 회원가입
4. 로그인
5. 사용자 조회
6. 인증 통합 테스트

---

## Phase 4 — Routine

1. 반복 루틴 CRUD
2. 반복 요일 처리
3. 날짜별 루틴 생성
4. DailyRoutine CRUD
5. 상태 변경
6. 실패 사유
7. Backend 핵심 흐름 통합 테스트

---

## Phase 5 — Frontend Core

1. React 프로젝트 구성
2. 디자인 시스템
3. 모바일 Layout
4. 인증 화면
5. 인증 상태 관리
6. Home
7. Routine
8. Calendar
9. Profile

---

## Phase 6 — Video

1. S3 구조 설계
2. S3 구축
3. Backend 영상 업로드
4. Video DB
5. 모바일 영상 촬영
6. 영상 길이 검증
7. 영상 업로드 UI
8. 영상 조회 / 재생

---

## Phase 7 — Daily Vlog

1. FFmpeg 환경 구성
2. 성공 영상 조회
3. 영상 시간순 정렬
4. 영상 병합
5. DailyVlog DB 저장
6. 상태 관리
7. Vlog 재생
8. E2E 테스트

---

## Phase 8 — Containerization

1. Backend Dockerfile
2. Frontend Production Build
3. Docker Compose
4. Nginx
5. 로컬 Docker 통합 테스트

---

## Phase 9 — AWS

1. VPC
2. Subnet
3. RDS
4. S3
5. ECR
6. App EC2
7. Management EC2
8. IAM
9. Secret / 환경변수
10. 최초 수동 배포
11. Domain
12. HTTPS

---

## Phase 10 — CI/CD

1. Jenkins Pipeline
2. GitHub Webhook
3. Build
4. Test
5. Docker Image
6. ECR Push
7. EC2 Deploy
8. Health Check
9. 안정적인 Container 교체
10. Rollback

---

## Phase 11 — Monitoring

1. Actuator
2. Micrometer
3. Prometheus
4. Node Exporter
5. cAdvisor
6. Grafana
7. API Monitoring
8. CloudWatch
9. Alert 기준 설정

---

## Phase 12 — Terraform

1. AWS Resource 코드화
2. Module 구조화
3. Environment 구조화
4. Remote State
5. Infrastructure 재현 테스트

---

## Phase 13 — Reliability Test

다음 장애 상황을 테스트한다.

* Application Down
* Database 장애
* CPU 과부하
* Memory 부족
* Disk 부족
* 영상 처리 실패
* Jenkins 배포 실패

배포 실패 시 기존 정상 버전이 유지되는지 검증한다.

---

# 13. 향후 확장

MVP와 초기 AWS 운영 구조가 안정된 이후 다음 기능을 검토한다.

## Infrastructure

```text
ALB
Scale-out
Auto Scaling
```

## Video Processing

```text
Message Queue
Video Worker
Asynchronous Processing
```

## Container Orchestration

```text
Kubernetes
EKS
Argo CD
GitOps
```

## Video Features

```text
자동 자막
영상 편집
영상 템플릿
영상 효과
```

---

# 14. 개발 원칙

프로젝트 개발 시 다음 원칙을 따른다.

1. 핵심 기능부터 구현한다.
2. 사용하지 않는 기능을 미리 구현하지 않는다.
3. MVP 범위를 불필요하게 확장하지 않는다.
4. Frontend와 Backend의 책임을 명확히 분리한다.
5. API 계약을 기준으로 양쪽을 개발한다.
6. 민감정보는 코드에 직접 저장하지 않는다.
7. 기능 구현 후 테스트한다.
8. 개발 과정에서 발생한 문제와 해결 과정을 기록한다.
9. 실제 필요한 시점에 Infrastructure를 확장한다.
10. 결과뿐 아니라 설계 및 문제 해결 근거를 문서화한다.

---

# 15. 프로젝트 완료 기준

최소 다음 흐름이 실제 Production 환경에서 정상적으로 동작해야 프로젝트의 핵심 기능이 완성된 것으로 본다.

```text
회원가입
→ 로그인
→ 반복 루틴 생성
→ 날짜별 루틴 생성
→ 영상 촬영
→ S3 업로드
→ 루틴 SUCCESS
→ Daily Vlog 생성
→ 영상 재생
→ Calendar 기록 확인
```

추가로 다음 운영 환경이 정상적으로 동작해야 한다.

```text
GitHub Push
→ Jenkins
→ Test
→ Docker Build
→ ECR
→ EC2 Deploy
→ Health Check
→ Monitoring
```
