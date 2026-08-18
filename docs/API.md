# Routine Log — API Specification

## 1. 목적

이 문서는 Routine Log Frontend와 Backend 사이의 API 계약을 정의한다.

Backend와 Frontend는 이 문서를 기준으로 구현한다.

API 구현이 변경되는 경우 코드와 함께 이 문서를 수정한다.

---

# 2. Base URL

모든 API는 다음 Prefix를 사용한다.

```text
/api/v1
```

예:

```text
POST /api/v1/auth/signup
GET  /api/v1/routines
```

로컬 개발 환경 예시:

```text
http://localhost:8080/api/v1
```

Production 환경에서는 동일 Domain에서 Nginx를 통해 `/api` 요청을 Backend로 전달한다.

---

# 3. Content Type

기본 Request / Response:

```http
Content-Type: application/json
```

영상 업로드:

```http
Content-Type: multipart/form-data
```

---

# 4. Authentication

인증 방식:

```text
JWT Bearer Token
```

인증이 필요한 API에서는 다음 Header를 사용한다.

```http
Authorization: Bearer {accessToken}
```

예:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

사용자 ID를 Request Body에서 전달하여 인증 용도로 사용하지 않는다.

현재 사용자는 JWT를 기준으로 Backend에서 판단한다.

---

# 5. Common Response Format

일반적인 성공 Response는 다음 구조를 사용한다.

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

목록 Response:

```json
{
  "success": true,
  "data": [],
  "error": null
}
```

---

# 6. Error Response

공통 오류 Response:

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

`code`는 Frontend에서 오류 종류를 구분할 수 있는 안정적인 값으로 사용한다.

`message`는 사용자 또는 개발자가 이해할 수 있는 설명이다.

---

# 7. HTTP Status

기본적으로 다음 HTTP Status를 사용한다.

| Status                    | 의미                        |
| ------------------------- | ------------------------- |
| 200 OK                    | 정상 조회 / 수정                |
| 201 Created               | Resource 생성 성공            |
| 204 No Content            | 삭제 성공 등 Response Body 불필요 |
| 400 Bad Request           | 입력값 오류                    |
| 401 Unauthorized          | 인증되지 않음                   |
| 403 Forbidden             | 해당 Resource 접근 권한 없음      |
| 404 Not Found             | Resource 없음               |
| 409 Conflict              | 이메일 중복 등 상태 충돌            |
| 500 Internal Server Error | 서버 내부 오류                  |

모든 API에 `200 OK`를 반환하지 않는다.

---

# 8. Date / Time Format

날짜:

```text
YYYY-MM-DD
```

예:

```text
2026-08-18
```

시간:

```text
HH:mm
```

예:

```text
07:30
```

Timestamp:

```text
ISO-8601
```

예:

```text
2026-08-18T15:30:00
```

---

# 9. Authentication API

## 9.1 회원가입

```http
POST /api/v1/auth/signup
```

인증:

```text
불필요
```

### Request

```json
{
  "email": "nayoung@example.com",
  "password": "password123!",
  "name": "나영"
}
```

### Validation

`email`

```text
필수
이메일 형식
최대 255자
중복 불가
```

`password`

```text
필수
```

초기 MVP에서는 비밀번호 정책을 과도하게 복잡하게 만들지 않는다.

`name`

```text
필수
최대 50자
```

### Response

Status:

```text
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "nayoung@example.com",
    "name": "나영"
  },
  "error": null
}
```

### Errors

```text
EMAIL_ALREADY_EXISTS
INVALID_EMAIL
INVALID_REQUEST
```

---

# 10. 로그인

```http
POST /api/v1/auth/login
```

인증:

```text
불필요
```

### Request

```json
{
  "email": "nayoung@example.com",
  "password": "password123!"
}
```

### Response

```text
200 OK
```

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  },
  "error": null
}
```

초기 MVP에서는 Access Token 기반으로 구현한다.

Refresh Token은 초기 범위에 포함하지 않는다.

### Errors

```text
INVALID_CREDENTIALS
INVALID_REQUEST
```

잘못된 이메일과 잘못된 비밀번호를 외부 Response에서 지나치게 구체적으로 구분하지 않는다.

---

# 11. 로그아웃

초기 JWT 방식에서는 Server Session을 사용하지 않는다.

Frontend에서 저장된 Access Token을 제거하는 방식으로 로그아웃한다.

따라서 초기 MVP에서는 별도 Logout API를 필수로 두지 않는다.

```text
Frontend
→ Token 삭제
→ 인증 상태 초기화
→ 시작 또는 로그인 화면 이동
```

Refresh Token 또는 Token Blacklist를 도입하는 경우 별도 Logout API를 추가한다.

---

# 12. User API

## 12.1 현재 사용자 조회

```http
GET /api/v1/users/me
```

인증:

```text
필수
```

### Response

```text
200 OK
```

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "nayoung@example.com",
    "name": "나영",
    "profileImageUrl": null,
    "createdAt": "2026-08-18T12:00:00"
  },
  "error": null
}
```

---

# 13. 사용자 정보 수정

```http
PATCH /api/v1/users/me
```

인증:

```text
필수
```

초기 MVP에서는 이름 수정부터 지원한다.

### Request

```json
{
  "name": "이나영"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "nayoung@example.com",
    "name": "이나영",
    "profileImageUrl": null
  },
  "error": null
}
```

---

# 14. 프로필 이미지

프로필 이미지 기능 구현 시 S3를 이용한다.

초기 API:

```http
POST /api/v1/users/me/profile-image
```

Content-Type:

```text
multipart/form-data
```

### Form Data

```text
file
```

### Response

```json
{
  "success": true,
  "data": {
    "profileImageUrl": "temporary-or-authorized-url"
  },
  "error": null
}
```

Database에는 공개 URL이 아니라 S3 Object Key 저장을 기본으로 한다.

---

# 15. Routine API

Routine은 반복 루틴 Template이다.

예:

```text
07:00 운동
월 / 화 / 수 / 목 / 금
```

---

# 16. 반복 루틴 생성

```http
POST /api/v1/routines
```

인증:

```text
필수
```

### Request

```json
{
  "title": "아침 운동",
  "scheduledTime": "07:00",
  "repeatDays": [
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY"
  ]
}
```

### Validation

`title`

```text
필수
최대 100자
```

`scheduledTime`

```text
필수
HH:mm
```

`repeatDays`

```text
필수
최소 1개
중복 불가
```

Allowed Values:

```text
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

### Response

```text
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": 10,
    "title": "아침 운동",
    "scheduledTime": "07:00",
    "repeatDays": [
      "MONDAY",
      "TUESDAY",
      "WEDNESDAY",
      "THURSDAY",
      "FRIDAY"
    ],
    "active": true
  },
  "error": null
}
```

---

# 17. 반복 루틴 목록 조회

```http
GET /api/v1/routines
```

인증:

```text
필수
```

기본적으로 활성 Routine만 반환한다.

### Response

```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "title": "아침 운동",
      "scheduledTime": "07:00",
      "repeatDays": [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY"
      ],
      "active": true
    },
    {
      "id": 11,
      "title": "독서",
      "scheduledTime": "22:00",
      "repeatDays": [
        "MONDAY",
        "WEDNESDAY",
        "FRIDAY"
      ],
      "active": true
    }
  ],
  "error": null
}
```

기본 정렬:

```text
scheduledTime ASC
id ASC
```

---

# 18. 반복 루틴 단건 조회

```http
GET /api/v1/routines/{routineId}
```

인증:

```text
필수
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 10,
    "title": "아침 운동",
    "scheduledTime": "07:00",
    "repeatDays": [
      "MONDAY",
      "TUESDAY",
      "WEDNESDAY",
      "THURSDAY",
      "FRIDAY"
    ],
    "active": true
  },
  "error": null
}
```

### Errors

```text
ROUTINE_NOT_FOUND
ROUTINE_ACCESS_DENIED
```

다른 사용자의 Routine을 ID만 알고 있다고 조회할 수 없어야 한다.

---

# 19. 반복 루틴 수정

```http
PATCH /api/v1/routines/{routineId}
```

인증:

```text
필수
```

### Request

```json
{
  "title": "아침 스트레칭",
  "scheduledTime": "07:30",
  "repeatDays": [
    "MONDAY",
    "WEDNESDAY",
    "FRIDAY"
  ]
}
```

PATCH이므로 수정하려는 값만 전달할 수 있도록 구현할 수 있다.

예:

```json
{
  "scheduledTime": "08:00"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 10,
    "title": "아침 스트레칭",
    "scheduledTime": "07:30",
    "repeatDays": [
      "MONDAY",
      "WEDNESDAY",
      "FRIDAY"
    ],
    "active": true
  },
  "error": null
}
```

Routine 수정은 이미 생성된 과거 DailyRoutine Snapshot을 변경하지 않는다.

---

# 20. 반복 루틴 삭제

```http
DELETE /api/v1/routines/{routineId}
```

인증:

```text
필수
```

MVP에서는 실제 Database Row 삭제보다 Soft Delete 성격의 비활성화를 사용한다.

내부 처리:

```text
active = false
```

### Response

```text
204 No Content
```

과거 DailyRoutine 기록은 유지한다.

---

# 21. DailyRoutine API

DailyRoutine은 특정 날짜에 실제 수행할 루틴이다.

예:

```text
2026-08-18
07:00 아침 운동
PENDING
```

---

# 22. 날짜별 DailyRoutine 조회

```http
GET /api/v1/daily-routines?date={date}
```

예:

```http
GET /api/v1/daily-routines?date=2026-08-18
```

인증:

```text
필수
```

이 API 호출 시 해당 날짜에 필요한 반복 Routine 기반 DailyRoutine을 생성한다.

처리 흐름:

```text
기존 DailyRoutine 조회
↓
활성 Routine 조회
↓
해당 날짜 요일 확인
↓
아직 없는 DailyRoutine 생성
↓
전체 조회
↓
시간순 반환
```

### Response

```json
{
  "success": true,
  "data": [
    {
      "id": 101,
      "routineId": 10,
      "routineDate": "2026-08-18",
      "title": "아침 운동",
      "scheduledTime": "07:00",
      "status": "PENDING",
      "failureReason": null,
      "video": null
    },
    {
      "id": 102,
      "routineId": 11,
      "routineDate": "2026-08-18",
      "title": "독서",
      "scheduledTime": "22:00",
      "status": "SUCCESS",
      "failureReason": null,
      "video": {
        "id": 500
      }
    }
  ],
  "error": null
}
```

정렬:

```text
scheduledTime ASC
id ASC
```

---

# 23. 일회성 DailyRoutine 추가

특정 날짜에만 사용할 루틴을 생성한다.

```http
POST /api/v1/daily-routines
```

인증:

```text
필수
```

### Request

```json
{
  "routineDate": "2026-08-18",
  "title": "은행 방문",
  "scheduledTime": "14:00"
}
```

이 경우:

```text
routineId = null
```

### Response

```text
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": 103,
    "routineId": null,
    "routineDate": "2026-08-18",
    "title": "은행 방문",
    "scheduledTime": "14:00",
    "status": "PENDING",
    "failureReason": null,
    "video": null
  },
  "error": null
}
```

---

# 24. DailyRoutine 수정

특정 날짜의 실제 루틴만 수정한다.

```http
PATCH /api/v1/daily-routines/{dailyRoutineId}
```

인증:

```text
필수
```

### Request

```json
{
  "title": "은행 및 우체국 방문",
  "scheduledTime": "15:00"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 103,
    "routineId": null,
    "routineDate": "2026-08-18",
    "title": "은행 및 우체국 방문",
    "scheduledTime": "15:00",
    "status": "PENDING",
    "failureReason": null,
    "video": null
  },
  "error": null
}
```

원본 반복 Routine이 연결되어 있어도 이 API는 해당 DailyRoutine Snapshot만 변경한다.

---

# 25. DailyRoutine 삭제

```http
DELETE /api/v1/daily-routines/{dailyRoutineId}
```

인증:

```text
필수
```

### Response

```text
204 No Content
```

다른 사용자의 DailyRoutine은 삭제할 수 없다.

Video가 연결된 SUCCESS DailyRoutine 삭제 정책은 구현 단계에서 제한하거나 별도 삭제 흐름으로 처리한다.

MVP에서는 이미 영상 인증된 루틴의 임의 삭제를 제한하는 방향을 우선한다.

---

# 26. DailyRoutine 상태 Enum

Allowed Values:

```text
PENDING
SUCCESS
FAILED
```

일반적인 사용자는 직접 `SUCCESS` 값을 보내지 않는다.

SUCCESS는 영상 인증 성공 흐름에서 처리한다.

---

# 27. DailyRoutine 실패 처리

```http
PATCH /api/v1/daily-routines/{dailyRoutineId}/failed
```

인증:

```text
필수
```

### Request

```json
{
  "failureReason": "야근으로 운동을 하지 못했습니다."
}
```

실패 사유는 선택 입력으로 둘 수 있다.

```json
{
  "failureReason": null
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 101,
    "routineDate": "2026-08-18",
    "title": "아침 운동",
    "scheduledTime": "07:00",
    "status": "FAILED",
    "failureReason": "야근으로 운동을 하지 못했습니다.",
    "video": null
  },
  "error": null
}
```

---

# 28. FAILED → PENDING 복구

사용자가 실패 처리를 취소할 수 있도록 다음 API를 사용한다.

```http
PATCH /api/v1/daily-routines/{dailyRoutineId}/pending
```

인증:

```text
필수
```

Request Body:

```text
없음
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 101,
    "status": "PENDING",
    "failureReason": null
  },
  "error": null
}
```

PENDING 상태로 돌아갈 때 `failureReason`은 제거한다.

---

# 29. SUCCESS 처리 정책

일반적인 성공 처리는 다음 API를 직접 호출하지 않는다.

```text
PATCH /daily-routines/{id}/success
```

대신:

```text
영상 업로드 성공
↓
Video 저장
↓
DailyRoutine SUCCESS
```

흐름을 사용한다.

영상 인증 없는 SUCCESS 처리는 MVP에서 허용하지 않는다.

---

# 30. Video API

Video는 DailyRoutine 성공 인증 영상이다.

---

# 31. 영상 업로드

```http
POST /api/v1/daily-routines/{dailyRoutineId}/videos
```

인증:

```text
필수
```

Content-Type:

```text
multipart/form-data
```

### Form Data

```text
file
```

예:

```text
file = routine-proof.mp4
```

### Frontend Validation

```text
영상 파일인지 확인
영상 길이 <= 15초
```

### Backend Validation

Backend에서도 반드시 검증한다.

```text
DailyRoutine 존재
사용자 소유권
지원 가능한 영상 파일
영상 길이 > 0
영상 길이 <= 15초
파일 크기 제한
```

### Processing

```text
파일 수신
↓
영상 Metadata 확인
↓
S3 Upload
↓
Video DB Save
↓
DailyRoutine SUCCESS
↓
Response
```

### Response

```text
201 Created
```

```json
{
  "success": true,
  "data": {
    "video": {
      "id": 500,
      "dailyRoutineId": 101,
      "durationSeconds": 12.4,
      "fileSize": 4819231
    },
    "dailyRoutine": {
      "id": 101,
      "status": "SUCCESS",
      "failureReason": null
    }
  },
  "error": null
}
```

---

# 32. Video Upload Errors

예상 Error Code:

```text
DAILY_ROUTINE_NOT_FOUND
DAILY_ROUTINE_ACCESS_DENIED
VIDEO_ALREADY_EXISTS
INVALID_VIDEO_FILE
VIDEO_TOO_LONG
VIDEO_TOO_LARGE
VIDEO_UPLOAD_FAILED
S3_UPLOAD_FAILED
```

---

# 33. 영상 정보 조회

```http
GET /api/v1/videos/{videoId}
```

인증:

```text
필수
```

### Response

```json
{
  "success": true,
  "data": {
    "id": 500,
    "dailyRoutineId": 101,
    "durationSeconds": 12.4,
    "fileSize": 4819231,
    "playbackUrl": "temporary-presigned-url"
  },
  "error": null
}
```

`playbackUrl`은 영구 Public URL이 아니라 제한된 접근 방식으로 제공한다.

Presigned URL 방식을 사용할 경우 만료 시간이 있는 URL을 반환한다.

---

# 34. Video Playback Policy

S3 Bucket은 Public으로 만들지 않는다.

기본 흐름:

```text
Frontend
↓
GET /videos/{videoId}
↓
Backend 소유권 검증
↓
Presigned URL 생성
↓
Frontend 영상 재생
```

---

# 35. DailyVlog API

DailyVlog는 특정 날짜의 SUCCESS 영상을 시간순으로 병합한 영상이다.

---

# 36. DailyVlog 생성

```http
POST /api/v1/vlogs
```

인증:

```text
필수
```

### Request

```json
{
  "date": "2026-08-18"
}
```

### Processing

초기 MVP에서는 다음 흐름으로 처리한다.

```text
날짜 검증
↓
SUCCESS DailyRoutine 조회
↓
Video 조회
↓
scheduledTime 순 정렬
↓
DailyVlog 생성 상태 PROCESSING
↓
S3 영상 다운로드
↓
FFmpeg 병합
↓
결과 영상 S3 Upload
↓
DailyVlog SUCCESS
```

### Response

동기 처리 구현 초기에는 완료된 결과를 반환할 수 있다.

```text
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": 300,
    "vlogDate": "2026-08-18",
    "status": "SUCCESS",
    "durationSeconds": 42.7,
    "playbackUrl": "temporary-presigned-url"
  },
  "error": null
}
```

향후 비동기 Worker 구조 도입 시 생성 요청 Response를 `PROCESSING`으로 변경할 수 있다.

---

# 37. DailyVlog 생성 조건

최소 하나 이상의 SUCCESS Video가 있어야 한다.

예:

```text
SUCCESS Video 0개
→ 생성 불가
```

Error:

```text
NO_VIDEOS_FOR_VLOG
```

같은 날짜 DailyVlog가 이미 존재하는 경우 기본적으로 기존 Record를 사용한다.

재생성 정책은 별도로 관리한다.

---

# 38. DailyVlog 날짜 조회

```http
GET /api/v1/vlogs?date={date}
```

예:

```http
GET /api/v1/vlogs?date=2026-08-18
```

인증:

```text
필수
```

### SUCCESS Response

```json
{
  "success": true,
  "data": {
    "id": 300,
    "vlogDate": "2026-08-18",
    "status": "SUCCESS",
    "durationSeconds": 42.7,
    "playbackUrl": "temporary-presigned-url"
  },
  "error": null
}
```

---

# 39. PROCESSING Response

```json
{
  "success": true,
  "data": {
    "id": 300,
    "vlogDate": "2026-08-18",
    "status": "PROCESSING",
    "durationSeconds": null,
    "playbackUrl": null
  },
  "error": null
}
```

---

# 40. FAILED Response

```json
{
  "success": true,
  "data": {
    "id": 300,
    "vlogDate": "2026-08-18",
    "status": "FAILED",
    "durationSeconds": null,
    "playbackUrl": null
  },
  "error": null
}
```

내부 `failureMessage`를 사용자에게 그대로 노출하지 않는다.

필요하면 사용자용 메시지와 내부 로그를 분리한다.

---

# 41. DailyVlog 없음

해당 날짜 DailyVlog가 생성되지 않았다면:

```text
404 Not Found
```

예:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "DAILY_VLOG_NOT_FOUND",
    "message": "해당 날짜의 브이로그가 없습니다."
  }
}
```

---

# 42. Calendar API Policy

초기 MVP에서는 별도의 `/calendar` Resource를 만들지 않는다.

캘린더 화면에서 날짜 선택 후:

```http
GET /api/v1/daily-routines?date=2026-08-18
```

및:

```http
GET /api/v1/vlogs?date=2026-08-18
```

를 사용한다.

월 전체의 상태 요약이 필요해지는 시점에 별도 Calendar Summary API를 추가한다.

---

# 43. Future Calendar Summary API

필요성이 생기면 다음 형태를 검토한다.

```http
GET /api/v1/daily-routines/summary?year=2026&month=8
```

예상 Response:

```json
{
  "success": true,
  "data": [
    {
      "date": "2026-08-18",
      "total": 5,
      "success": 4,
      "failed": 1,
      "pending": 0,
      "hasVlog": true
    }
  ],
  "error": null
}
```

초기 API 구현 단계에서는 필수가 아니다.

---

# 44. Core Error Codes

## Authentication

```text
INVALID_CREDENTIALS
INVALID_TOKEN
EXPIRED_TOKEN
AUTHENTICATION_REQUIRED
```

## User

```text
USER_NOT_FOUND
EMAIL_ALREADY_EXISTS
```

## Routine

```text
ROUTINE_NOT_FOUND
ROUTINE_ACCESS_DENIED
INVALID_REPEAT_DAY
```

## DailyRoutine

```text
DAILY_ROUTINE_NOT_FOUND
DAILY_ROUTINE_ACCESS_DENIED
DAILY_ROUTINE_ALREADY_EXISTS
INVALID_DAILY_ROUTINE_STATUS
```

## Video

```text
VIDEO_NOT_FOUND
VIDEO_ACCESS_DENIED
VIDEO_ALREADY_EXISTS
INVALID_VIDEO_FILE
VIDEO_TOO_LONG
VIDEO_TOO_LARGE
VIDEO_UPLOAD_FAILED
```

## Vlog

```text
DAILY_VLOG_NOT_FOUND
NO_VIDEOS_FOR_VLOG
VLOG_ALREADY_PROCESSING
VLOG_GENERATION_FAILED
```

## Common

```text
INVALID_REQUEST
METHOD_NOT_ALLOWED
INTERNAL_SERVER_ERROR
```

---

# 45. Validation Error

Request Validation 실패 Response 예:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "입력값이 올바르지 않습니다.",
    "fields": {
      "email": "올바른 이메일 형식이 아닙니다.",
      "name": "이름은 필수입니다."
    }
  }
}
```

Validation Error에서 `fields`는 선택적으로 포함할 수 있다.

---

# 46. Authentication Failure

Token이 없는 경우:

```text
401 Unauthorized
```

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "message": "로그인이 필요합니다."
  }
}
```

---

# 47. Invalid Token

```text
401 Unauthorized
```

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "유효하지 않은 인증 정보입니다."
  }
}
```

---

# 48. Access Denied

다른 사용자의 Resource에 접근한 경우:

```text
403 Forbidden
```

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ROUTINE_ACCESS_DENIED",
    "message": "해당 루틴에 접근할 권한이 없습니다."
  }
}
```

Resource 존재 여부 노출을 줄이기 위해 필요에 따라 404 정책을 검토할 수 있지만, MVP에서는 일관성 있는 403 정책을 우선한다.

---

# 49. API Ownership Rules

다음 Resource는 항상 현재 로그인 사용자 소유권을 검증한다.

```text
Routine
DailyRoutine
Video
DailyVlog
```

예:

```text
JWT User ID = 1

Routine.user.id = 2

→ 접근 거부
```

---

# 50. API Endpoint Summary

## Auth

| Method | Endpoint              | 설명   | 인증 |
| ------ | --------------------- | ---- | -- |
| POST   | `/api/v1/auth/signup` | 회원가입 | X  |
| POST   | `/api/v1/auth/login`  | 로그인  | X  |

---

## User

| Method | Endpoint                         | 설명         | 인증 |
| ------ | -------------------------------- | ---------- | -- |
| GET    | `/api/v1/users/me`               | 내 정보 조회    | O  |
| PATCH  | `/api/v1/users/me`               | 내 정보 수정    | O  |
| POST   | `/api/v1/users/me/profile-image` | 프로필 이미지 수정 | O  |

---

## Routine

| Method | Endpoint                | 설명            | 인증 |
| ------ | ----------------------- | ------------- | -- |
| POST   | `/api/v1/routines`      | 반복 루틴 생성      | O  |
| GET    | `/api/v1/routines`      | 반복 루틴 목록      | O  |
| GET    | `/api/v1/routines/{id}` | 반복 루틴 단건      | O  |
| PATCH  | `/api/v1/routines/{id}` | 반복 루틴 수정      | O  |
| DELETE | `/api/v1/routines/{id}` | 반복 루틴 삭제/비활성화 | O  |

---

## DailyRoutine

| Method | Endpoint                              | 설명            | 인증 |
| ------ | ------------------------------------- | ------------- | -- |
| GET    | `/api/v1/daily-routines?date=`        | 날짜별 루틴 조회     | O  |
| POST   | `/api/v1/daily-routines`              | 일회성 루틴 생성     | O  |
| PATCH  | `/api/v1/daily-routines/{id}`         | 날짜별 루틴 수정     | O  |
| DELETE | `/api/v1/daily-routines/{id}`         | 날짜별 루틴 삭제     | O  |
| PATCH  | `/api/v1/daily-routines/{id}/failed`  | 실패 처리         | O  |
| PATCH  | `/api/v1/daily-routines/{id}/pending` | 실패 취소 / 대기 복구 | O  |

---

## Video

| Method | Endpoint                             | 설명             | 인증 |
| ------ | ------------------------------------ | -------------- | -- |
| POST   | `/api/v1/daily-routines/{id}/videos` | 인증 영상 업로드      | O  |
| GET    | `/api/v1/videos/{id}`                | 영상 정보 / 재생 URL | O  |

---

## DailyVlog

| Method | Endpoint              | 설명          | 인증 |
| ------ | --------------------- | ----------- | -- |
| POST   | `/api/v1/vlogs`       | 브이로그 생성     | O  |
| GET    | `/api/v1/vlogs?date=` | 날짜별 브이로그 조회 | O  |

---

# 51. Core User Flow API

전체 핵심 흐름:

```text
POST /auth/signup
↓
POST /auth/login
↓
GET /users/me
↓
POST /routines
↓
GET /daily-routines?date=2026-08-18
↓
POST /daily-routines/{id}/videos
↓
DailyRoutine SUCCESS
↓
POST /vlogs
↓
GET /vlogs?date=2026-08-18
```

실패한 경우:

```text
GET /daily-routines?date=2026-08-18
↓
PATCH /daily-routines/{id}/failed
↓
Calendar에서 FAILED 기록 조회
```

---

# 52. API Implementation Priority

Backend에서는 다음 순서로 구현한다.

```text
1. Common Response / Exception
2. Signup
3. Login
4. JWT Authentication
5. GET /users/me
6. Routine CRUD
7. DailyRoutine Generation
8. DailyRoutine CRUD
9. FAILED / PENDING 상태 변경
10. Video Upload
11. Video Playback
12. DailyVlog
```

---

# 53. API Test Flow

최소 다음 통합 테스트를 구현한다.

## Authentication

```text
회원가입
→ 로그인
→ JWT 발급
→ 인증 API 성공
```

## Authorization

```text
User A 로그인
→ User B Resource 접근
→ 거부
```

## Routine

```text
Routine 생성
→ 조회
→ 수정
→ 비활성화
```

## DailyRoutine

```text
Routine 생성
→ 해당 반복 요일 날짜 조회
→ DailyRoutine 자동 생성
→ 같은 날짜 재조회
→ 중복 생성되지 않음
```

## Failed

```text
PENDING
→ FAILED
→ failureReason 저장
→ PENDING 복구
→ failureReason null
```

## Video

```text
PENDING DailyRoutine
→ 영상 업로드
→ Video 생성
→ DailyRoutine SUCCESS
```

## Vlog

```text
SUCCESS Video 여러 개
→ DailyVlog 생성
→ 시간순 병합
→ SUCCESS
```

---

# 54. API Change Rules

API 변경 시 다음 파일을 함께 확인한다.

```text
docs/API.md

Backend DTO
Backend Controller
Backend Test

Frontend Type
Frontend API Module
Frontend UI
```

Database 구조까지 변경되는 경우:

```text
docs/ERD.md
```

Architecture 자체가 변경되는 경우:

```text
docs/ARCHITECTURE.md
```

도 함께 갱신한다.

---

# 55. MVP API Principles

1. API는 Resource 중심으로 설계한다.
2. 현재 사용자 ID는 JWT에서 얻는다.
3. Entity를 직접 Response로 반환하지 않는다.
4. Request / Response DTO를 사용한다.
5. 모든 사용자 Resource는 소유권을 검증한다.
6. SUCCESS는 영상 인증과 연결한다.
7. 반복 Routine과 DailyRoutine을 명확히 구분한다.
8. 과거 DailyRoutine Snapshot을 보존한다.
9. 영상 파일 자체는 S3에 저장한다.
10. API 구조를 초기부터 과도하게 확장하지 않는다.
11. Frontend와 Backend가 동일한 API 명세를 사용한다.
12. 실제 구현과 문서가 달라지면 즉시 문서를 수정한다.
