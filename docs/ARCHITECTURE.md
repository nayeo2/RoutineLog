# Routine Log — Architecture

## 1. 목적

이 문서는 Routine Log의 전체 시스템 구조와 각 구성 요소의 책임을 정의한다.

Routine Log는 다음 구성으로 시작한다.

```text
Frontend
→ Backend API
→ Database

Backend
→ S3
→ FFmpeg

Infrastructure
→ Docker
→ AWS
→ Jenkins
→ Monitoring
```

초기에는 단순하고 운영 가능한 구조를 우선하고, 필요성이 확인된 이후 Scale-out 또는 Kubernetes 구조로 확장한다.

---

# 2. High-Level Architecture

전체 서비스 흐름은 다음과 같다.

```text
User
  ↓
Web Browser
  ↓
React Frontend
  ↓
Nginx
  ↓
Spring Boot API
  ├─ MySQL
  ├─ AWS S3
  └─ FFmpeg
```

운영 환경에서는 다음과 같이 구성한다.

```text
Internet
   ↓
Route 53
   ↓
HTTPS
   ↓
App EC2
├─ Nginx
├─ React Frontend
└─ Spring Boot Backend
      ├─ RDS MySQL
      └─ S3

Management EC2
├─ Jenkins
├─ Prometheus
└─ Grafana

AWS
└─ CloudWatch
```

---

# 3. Repository Architecture

프로젝트는 Monorepo 구조를 사용한다.

```text
routine-log/
├─ backend/
├─ frontend/
├─ docs/
├─ infra/
├─ monitoring/
├─ nginx/
├─ AGENTS.md
├─ docker-compose.yml
└─ README.md
```

각 디렉터리 역할:

| 디렉터리       | 역할                             |
| ---------- | ------------------------------ |
| backend    | Spring Boot API 서버             |
| frontend   | React 웹 애플리케이션                 |
| docs       | 설계 및 운영 문서                     |
| infra      | Terraform 기반 AWS IaC           |
| monitoring | Prometheus / Grafana 설정        |
| nginx      | Reverse Proxy 및 Frontend 제공 설정 |

---

# 4. Frontend Architecture

Frontend는 React + TypeScript + Vite 기반으로 구현한다.

```text
frontend/
└─ src/
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

## 4.1 Pages

Route 단위 화면을 관리한다.

예:

```text
pages/
├─ StartPage
├─ LoginPage
├─ SignupPage
├─ HomePage
├─ CalendarPage
├─ RoutinePage
└─ ProfilePage
```

Page는 화면 조합을 담당하고, 복잡한 Domain 로직을 직접 구현하지 않는다.

---

## 4.2 Features

Domain별 기능을 관리한다.

```text
features/
├─ auth/
├─ routine/
├─ dailyRoutine/
├─ video/
├─ vlog/
└─ user/
```

예:

```text
features/routine/
├─ api.ts
├─ types.ts
├─ hooks.ts
└─ components/
```

---

## 4.3 Components

여러 화면에서 재사용하는 공통 UI를 관리한다.

예:

```text
components/
├─ Button
├─ Card
├─ Modal
├─ BottomSheet
├─ BottomNavigation
└─ Loading
```

---

# 5. Frontend Request Flow

Frontend에서 Backend API를 호출하는 기본 흐름:

```text
Page
↓
Feature Hook / Event Handler
↓
API Module
↓
API Client
↓
Spring Boot
```

예:

```text
HomePage
↓
useDailyRoutines()
↓
dailyRoutineApi
↓
apiClient
↓
GET /api/v1/daily-routines
```

각 Component에서 직접 API URL과 Authorization Header를 반복 작성하지 않는다.

---

# 6. Authentication Architecture

인증은 Spring Security + JWT 기반으로 구성한다.

기본 흐름:

```text
Login Request
↓
Spring Security
↓
User 인증
↓
JWT 발급
↓
Frontend 저장
↓
Authorization Header
↓
Protected API
```

요청:

```text
Authorization: Bearer {accessToken}
```

Backend에서는 JWT 검증 후 인증 사용자 정보를 Security Context에 저장한다.

---

# 7. Authentication Flow

```text
사용자
↓
POST /api/v1/auth/login
↓
AuthController
↓
AuthService
↓
UserRepository
↓
Password 검증
↓
JWT 생성
↓
Frontend
```

이후 인증 API:

```text
Frontend
↓
Authorization: Bearer JWT
↓
JWT Filter
↓
JWT 검증
↓
SecurityContext
↓
Controller
```

---

# 8. Backend Architecture

Backend는 Spring Boot 기반 Layered Architecture를 사용한다.

기본 구조:

```text
Controller
↓
Service
↓
Repository
↓
Database
```

Domain별 Package:

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

---

# 9. Backend Domain Structure

Domain 내부는 다음 구조를 기본으로 한다.

```text
routine/
├─ controller/
├─ service/
├─ repository/
├─ domain/
└─ dto/
```

각 Layer 역할:

| Layer      | 역할                      |
| ---------- | ----------------------- |
| Controller | HTTP Request / Response |
| Service    | 비즈니스 로직                 |
| Repository | DB 접근                   |
| Domain     | Entity / Enum           |
| DTO        | API 입력 / 출력             |

---

# 10. Backend Request Flow

일반 API 호출 흐름:

```text
HTTP Request
↓
Controller
↓
Request DTO Validation
↓
Service
↓
Repository
↓
MySQL
↓
Service
↓
Response DTO
↓
Controller
↓
HTTP Response
```

---

# 11. Database Architecture

Database는 MySQL을 사용한다.

로컬 개발:

```text
Spring Boot
↓
Local MySQL
```

운영:

```text
Spring Boot
↓
AWS RDS MySQL
```

주요 Entity:

```text
User
Routine
DailyRoutine
Video
DailyVlog
```

Entity 상세 관계는 `docs/ERD.md`에서 관리한다.

---

# 12. Routine Architecture

Routine은 반복 루틴의 Template 역할을 한다.

예:

```text
Routine

07:00 운동
월 / 화 / 수 / 목 / 금
```

이 데이터 자체는 실제 하루의 수행 결과를 의미하지 않는다.

---

# 13. DailyRoutine Architecture

DailyRoutine은 특정 날짜에 실제 수행할 루틴이다.

```text
Routine
↓
날짜 조회
↓
해당 요일 반복 여부 확인
↓
DailyRoutine 생성
```

예:

```text
Routine
07:00 운동
월~금

↓

2026-08-18

↓

DailyRoutine
2026-08-18 07:00 운동
PENDING
```

DailyRoutine은 이후 성공 또는 실패 상태를 갖는다.

```text
PENDING
SUCCESS
FAILED
```

---

# 14. DailyRoutine Generation Strategy

특정 날짜의 루틴을 조회할 때 다음 순서로 처리한다.

```text
사용자가 날짜 선택
↓
해당 날짜 DailyRoutine 조회
↓
반복 Routine 조회
↓
해당 요일에 적용되는 Routine 확인
↓
아직 생성되지 않은 항목만 DailyRoutine 생성
↓
전체 DailyRoutine 조회
↓
시간순 반환
```

중복 생성되지 않도록 제약을 둔다.

예:

```text
routine_id + routine_date
```

조합 기준 중복 방지를 검토한다.

---

# 15. Routine Modification Policy

반복 Routine 수정과 이미 생성된 DailyRoutine은 분리해서 관리한다.

기본 원칙:

```text
Routine 변경
→ 미래 날짜의 기준 변경

이미 생성된 과거 DailyRoutine
→ 기존 기록 유지
```

과거 기록을 반복 Routine 수정으로 자동 덮어쓰지 않는다.

---

# 16. Video Architecture

Video는 특정 DailyRoutine의 영상 인증 파일이다.

관계:

```text
DailyRoutine
↓
Video
```

실제 영상 파일은 Database가 아닌 S3에 저장한다.

```text
Video DB
├─ id
├─ dailyRoutineId
├─ objectKey
├─ duration
├─ fileSize
└─ metadata
```

파일:

```text
AWS S3
└─ actual video file
```

---

# 17. Video Upload Flow

기본 영상 업로드 흐름:

```text
사용자 루틴 선택
↓
카메라 촬영 / 기존 영상 선택
↓
Frontend 15초 검증
↓
Backend Upload API
↓
Backend Validation
↓
S3 업로드
↓
Video Metadata DB 저장
↓
DailyRoutine SUCCESS
↓
Response
```

Frontend 검증만 신뢰하지 않는다.

Backend에서도 반드시 파일과 영상 길이를 검증한다.

---

# 18. Video Success Transaction

영상 업로드와 성공 상태는 논리적으로 하나의 작업 흐름으로 관리한다.

```text
Video Upload
↓
S3 Upload Success
↓
Video DB Save
↓
DailyRoutine SUCCESS
```

S3 업로드에 실패하면 DailyRoutine을 성공 상태로 변경하지 않는다.

DB 저장 실패 시 이미 업로드된 S3 Object 정리 여부를 고려한다.

---

# 19. S3 Architecture

S3에는 두 종류의 영상 데이터를 저장한다.

```text
Routine Proof Videos
Daily Vlog Videos
```

예상 Object 구조:

```text
users/
└─ {userId}/
   ├─ routine-videos/
   │  └─ {yyyy}/{MM}/{dd}/
   │     └─ {uuid}.mp4
   │
   └─ daily-vlogs/
      └─ {yyyy}/{MM}/
         └─ {yyyy-MM-dd}-{uuid}.mp4
```

예:

```text
users/15/routine-videos/2026/08/18/9f3e....mp4
```

Daily Vlog:

```text
users/15/daily-vlogs/2026/08/2026-08-18-a83f....mp4
```

Database에는 Bucket 전체 URL보다 Object Key 저장을 기본으로 한다.

---

# 20. Video Access

S3 Bucket은 Public Access를 기본으로 사용하지 않는다.

```text
Browser
↓
Backend
↓
Authorization 확인
↓
S3 접근
```

영상 조회 방식은 구현 단계에서 다음 중 적절한 방식을 결정한다.

```text
Backend Streaming

또는

Presigned URL
```

초기에는 S3 Object를 공개 URL로 만들지 않는다.

---

# 21. Daily Vlog Architecture

Daily Vlog는 사용자의 하루 성공 영상을 하나로 병합한 결과이다.

기본 흐름:

```text
특정 날짜
↓
SUCCESS DailyRoutine 조회
↓
각 Video 조회
↓
루틴 시간순 정렬
↓
영상 파일 확보
↓
FFmpeg
↓
영상 병합
↓
S3 업로드
↓
DailyVlog DB 저장
```

---

# 22. Daily Vlog Processing State

DailyVlog는 영상 처리 실패 가능성을 고려하여 상태를 관리한다.

```text
PROCESSING
SUCCESS
FAILED
```

흐름:

```text
생성 요청
↓
PROCESSING
↓
FFmpeg 실행
├─ 성공 → SUCCESS
└─ 실패 → FAILED
```

---

# 23. FFmpeg Architecture

초기 MVP에서는 FFmpeg를 Spring Boot 애플리케이션 환경에서 직접 실행한다.

```text
Spring Boot
↓
SUCCESS Video 조회
↓
Temporary Directory
↓
S3 Video Download
↓
FFmpeg
↓
Merged Video
↓
S3 Upload
↓
Temporary File Delete
```

초기에는 별도 Video Worker를 만들지 않는다.

---

# 24. Temporary Video Files

FFmpeg 작업용 임시 파일은 영구 저장하지 않는다.

예:

```text
/tmp/routine-log/
└─ {jobId}/
   ├─ input-1.mp4
   ├─ input-2.mp4
   ├─ input-3.mp4
   └─ output.mp4
```

처리 완료 후 가능한 한 정리한다.

---

# 25. Future Video Worker Architecture

영상 처리 부하가 커지는 경우 다음 구조로 확장할 수 있다.

```text
Spring Boot API
↓
Message Queue
↓
Video Worker
↓
FFmpeg
↓
S3
```

예:

```text
API
↓
Queue
↓
Worker #1
↓
Worker #2
```

하지만 초기 MVP에서는 구현하지 않는다.

---

# 26. Local Development Architecture

로컬 개발 초기 구조:

```text
Browser
↓
Vite Development Server

React
↓
Spring Boot
↓
Local MySQL
```

예상 Port:

```text
Frontend
5173

Backend
8080

MySQL
3306
```

---

# 27. Local Environment Variables

환경별 설정을 코드와 분리한다.

Backend 예:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET

AWS_REGION
S3_BUCKET_NAME
```

Frontend 예:

```text
VITE_API_BASE_URL
```

실제 Secret은 Git에 Commit하지 않는다.

---

# 28. Spring Profiles

환경에 따라 Spring Profile을 분리한다.

예:

```text
application.yml
application-local.yml
application-prod.yml
```

기본 구조:

```text
application.yml
└─ 공통 설정

application-local.yml
└─ Local 환경 설정

application-prod.yml
└─ AWS 운영 환경 설정
```

민감정보는 설정 파일 자체에 직접 저장하지 않는다.

---

# 29. Docker Architecture

Production에서는 Backend와 Frontend를 Container 또는 Production Build 형태로 운영한다.

초기 Docker 구성:

```text
docker-compose
├─ nginx
├─ backend
└─ optional local db
```

운영 AWS에서는 RDS를 사용하므로 MySQL Container를 사용하지 않는다.

---

# 30. Nginx Architecture

Nginx 역할:

```text
Frontend Static File 제공

+

/api Request
↓
Spring Boot
```

예:

```text
/
→ React

/api/
→ Spring Boot:8080
```

이를 통해 사용자에게 하나의 Domain으로 서비스를 제공한다.

---

# 31. Initial AWS Architecture

초기 AWS 환경:

```text
VPC
├─ Public Subnet
│  ├─ App EC2
│  └─ Management EC2
│
└─ Private Subnet
   └─ RDS MySQL
```

추가 Resource:

```text
S3
ECR
Route 53
ACM
CloudWatch
IAM
Security Groups
```

---

# 32. App EC2

App EC2 역할:

```text
Nginx
React
Spring Boot
FFmpeg
```

초기에는 Application Server 한 대로 시작한다.

---

# 33. Management EC2

Management EC2 역할:

```text
Jenkins
Prometheus
Grafana
```

Application 서비스와 운영 도구를 분리한다.

---

# 34. RDS Architecture

RDS는 Private Subnet에 배치한다.

```text
Internet
X
↓
RDS
```

App EC2에서만 필요한 Database Port 접근을 허용한다.

예:

```text
App EC2 Security Group
↓
3306
↓
RDS Security Group
```

IP 전체 공개 방식은 사용하지 않는다.

---

# 35. Security Group Principle

Security Group은 필요한 통신만 허용한다.

예:

```text
Internet
→ 443
→ App EC2

App EC2
→ 3306
→ RDS

Management EC2
→ Monitoring Port
→ App EC2
```

`0.0.0.0/0` 전체 허용은 사용자 웹 접근에 필요한 Port 외에는 최소화한다.

---

# 36. CI/CD Architecture

기본 Pipeline:

```text
Developer
↓
GitHub Push
↓
Jenkins Webhook
↓
Jenkins
↓
Backend Test
↓
Frontend Build
↓
Docker Build
↓
ECR Push
↓
App EC2 Deploy
↓
Health Check
```

---

# 37. Deployment Flow

Backend 기준:

```text
GitHub
↓
Jenkins
↓
./gradlew test
↓
./gradlew build
↓
Docker Build
↓
ECR Push
↓
EC2 Pull
↓
New Container Start
↓
Health Check
↓
Old Container Stop
```

---

# 38. Deployment Safety

새 버전을 먼저 정상 실행한 후 기존 버전을 교체하는 구조를 목표로 한다.

```text
Current Container
= 정상 서비스 중

New Container
↓
Start
↓
Health Check
├─ SUCCESS
│   ↓
│ 기존 Container 종료
│
└─ FAILED
    ↓
    New Container 제거
    ↓
    기존 Container 유지
```

---

# 39. Health Check

Spring Boot Actuator를 사용한다.

예:

```text
/actuator/health
```

외부 API에서는 필요하다면 Nginx를 통해 제한된 Health Endpoint를 제공한다.

Health Check는 CI/CD 배포 성공 판단에도 사용한다.

---

# 40. Monitoring Architecture

Application:

```text
Spring Boot
↓
Actuator + Micrometer
↓
Prometheus
↓
Grafana
```

EC2:

```text
EC2
↓
Node Exporter
↓
Prometheus
```

Docker:

```text
Docker
↓
cAdvisor
↓
Prometheus
```

AWS Resource:

```text
EC2
RDS
↓
CloudWatch
```

---

# 41. Monitoring Targets

주요 모니터링 항목:

## Application

```text
API Response Time
Request Count
Error Rate
HTTP 5xx
JVM Memory
JVM Thread
```

## EC2

```text
CPU
Memory
Disk
Network
```

## Docker

```text
Container CPU
Container Memory
Container Restart
```

## RDS

```text
CPU
Connection Count
Storage
Latency
```

---

# 42. Alert Targets

향후 주요 Alert 기준:

```text
Application Down
High CPU
High Memory
Low Disk
HTTP 5xx 증가
Database Connection 문제
RDS CPU 증가
RDS Storage 부족
```

실제 임계값은 운영 테스트 후 결정한다.

---

# 43. Terraform Architecture

Terraform 구조:

```text
infra/
└─ terraform/
   ├─ modules/
   │  ├─ vpc/
   │  ├─ ec2/
   │  ├─ rds/
   │  ├─ s3/
   │  ├─ ecr/
   │  ├─ iam/
   │  └─ security-group/
   │
   └─ environments/
      └─ prod/
```

---

# 44. Terraform State

Terraform State는 Local Repository에 Commit하지 않는다.

향후 Remote State 사용:

```text
Terraform
↓
S3 Backend
```

State Lock이 필요하면 도입 시점의 Terraform/AWS 권장 방식에 따라 구성한다.

---

# 45. Network Architecture Evolution

초기:

```text
Internet
↓
App EC2
```

서비스 사용량 증가 시 검토:

```text
Internet
↓
ALB
├─ App EC2 #1
└─ App EC2 #2
```

확장 필요성이 실제로 확인된 이후 도입한다.

---

# 46. Kubernetes Evolution

초기 서비스에서는 Kubernetes를 사용하지 않는다.

향후 필요 시:

```text
GitHub
↓
CI
↓
ECR
↓
Argo CD
↓
EKS
```

구조로 확장 가능하다.

초기 Architecture와 Kubernetes Architecture를 동시에 구현하지 않는다.

---

# 47. Security Architecture Principles

서비스 전체에서 다음 원칙을 적용한다.

1. Database를 Internet에 직접 공개하지 않는다.
2. S3 Bucket을 Public으로 만들지 않는다.
3. 사용자 Resource 접근 시 소유권을 검증한다.
4. JWT Secret을 코드에 저장하지 않는다.
5. AWS Credential을 Repository에 저장하지 않는다.
6. HTTPS를 사용한다.
7. Security Group은 최소 권한 원칙을 적용한다.
8. IAM 역시 최소 권한 원칙을 적용한다.
9. Production Secret은 환경변수 또는 AWS Secret 관리 방식을 사용한다.

---

# 48. Failure Scenarios

Architecture 검증 시 다음 장애 상황을 고려한다.

## Application

```text
Spring Boot Down
Container Down
Application Health Check Failed
```

## Database

```text
RDS Connection Failed
Connection Pool Exhausted
```

## Storage

```text
S3 Upload Failed
S3 Download Failed
```

## Video

```text
Invalid Video
FFmpeg Failed
Temporary Disk Full
```

## Deployment

```text
Build Failed
Test Failed
Container Failed
Health Check Failed
```

---

# 49. Architecture Decision Principles

새로운 기술이나 구조를 도입할 때 다음 순서로 판단한다.

```text
현재 문제가 존재하는가?
↓
기존 구조로 해결 가능한가?
↓
새 기술이 실제 문제를 해결하는가?
↓
운영 복잡도 증가보다 이점이 큰가?
↓
도입
```

기술을 사용하기 위해 문제를 만들지 않는다.

---

# 50. Initial Architecture Summary

초기 최종 목표 구조:

```text
                       Internet
                          │
                       Route 53
                          │
                        HTTPS
                          │
                     ┌─────────┐
                     │ App EC2 │
                     ├─────────┤
                     │  Nginx  │
                     │  React  │
                     │ Spring  │
                     │ FFmpeg  │
                     └────┬────┘
                          │
               ┌──────────┴──────────┐
               │                     │
             RDS MySQL              S3
               │
        Private Subnet


                  Management EC2
                 ┌──────────────┐
                 │   Jenkins    │
                 │ Prometheus   │
                 │   Grafana    │
                 └──────────────┘

                       CloudWatch
```

이 구조를 기준으로 MVP를 완성한다.

이후 실제 트래픽, 장애 테스트, 영상 처리 부하를 확인한 후 필요한 부분만 확장한다.
