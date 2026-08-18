# Routine Log — ERD

## 1. 목적

이 문서는 Routine Log의 Database Entity 구조와 관계를 정의한다.

초기 MVP에서는 다음 Entity를 사용한다.

```text
User
Routine
RoutineRepeatDay
DailyRoutine
Video
DailyVlog
```

Routine과 DailyRoutine은 반드시 분리한다.

```text
Routine
= 반복 루틴 Template

DailyRoutine
= 특정 날짜에 실제로 수행할 루틴 기록
```

---

# 2. Entity Relationship Overview

전체 관계:

```text
User
├─ 1:N Routine
├─ 1:N DailyRoutine
├─ 1:N DailyVlog
│
Routine
├─ 1:N RoutineRepeatDay
└─ 1:N DailyRoutine
│
DailyRoutine
└─ 1:0..1 Video
```

관계 요약:

| Parent       | Child            | 관계     |
| ------------ | ---------------- | ------ |
| User         | Routine          | 1:N    |
| User         | DailyRoutine     | 1:N    |
| User         | DailyVlog        | 1:N    |
| Routine      | RoutineRepeatDay | 1:N    |
| Routine      | DailyRoutine     | 1:N    |
| DailyRoutine | Video            | 1:0..1 |

---

# 3. User

사용자 계정 정보를 저장한다.

## Table

```text
users
```

## Fields

| Column            | Type         | Nullable | Constraint | 설명                    |
| ----------------- | ------------ | -------: | ---------- | --------------------- |
| id                | BIGINT       |       NO | PK         | 사용자 ID                |
| email             | VARCHAR(255) |       NO | UNIQUE     | 로그인 이메일               |
| password          | VARCHAR(255) |       NO |            | 암호화된 비밀번호             |
| name              | VARCHAR(50)  |       NO |            | 사용자 이름                |
| profile_image_key | VARCHAR(500) |      YES |            | 프로필 이미지 S3 Object Key |
| created_at        | DATETIME     |       NO |            | 생성 시간                 |
| updated_at        | DATETIME     |       NO |            | 수정 시간                 |

## Constraints

```text
email UNIQUE
```

이메일은 로그인 식별자로 사용한다.

비밀번호는 평문 저장하지 않는다.

```text
Raw Password
↓
PasswordEncoder
↓
Encoded Password
↓
Database
```

---

# 4. Routine

사용자가 반복적으로 수행할 루틴의 Template을 저장한다.

예:

```text
07:00 운동
매주 월 / 화 / 수 / 목 / 금
```

Routine 자체는 특정 날짜의 성공/실패 기록을 저장하지 않는다.

## Table

```text
routines
```

## Fields

| Column         | Type         | Nullable | Constraint | 설명         |
| -------------- | ------------ | -------: | ---------- | ---------- |
| id             | BIGINT       |       NO | PK         | Routine ID |
| user_id        | BIGINT       |       NO | FK         | 소유 사용자     |
| title          | VARCHAR(100) |       NO |            | 루틴 내용      |
| scheduled_time | TIME         |       NO |            | 기본 수행 시간   |
| active         | BOOLEAN      |       NO |            | 반복 활성 여부   |
| created_at     | DATETIME     |       NO |            | 생성 시간      |
| updated_at     | DATETIME     |       NO |            | 수정 시간      |

## Default

```text
active = true
```

## Relationship

```text
User 1:N Routine
```

각 Routine은 반드시 하나의 User에 속한다.

---

# 5. RoutineRepeatDay

Routine의 반복 요일을 저장한다.

초기 설계에서는 반복 요일을 별도 Table로 관리한다.

## Table

```text
routine_repeat_days
```

## Fields

| Column      | Type        | Nullable | Constraint | 설명         |
| ----------- | ----------- | -------: | ---------- | ---------- |
| id          | BIGINT      |       NO | PK         | 반복 요일 ID   |
| routine_id  | BIGINT      |       NO | FK         | Routine ID |
| day_of_week | VARCHAR(20) |       NO |            | 반복 요일      |

## DayOfWeek Enum

```text
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

## Unique Constraint

하나의 Routine에 동일 요일이 두 번 등록되지 않도록 한다.

```text
UNIQUE (routine_id, day_of_week)
```

예:

```text
Routine #10

MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
```

---

# 6. RoutineRepeatDay 설계 이유

반복 요일을 다음과 같이 하나의 문자열로 저장하지 않는다.

```text
MON,TUE,WED
```

이 방식은 조회 및 수정 시 불편하다.

대신 관계형 구조를 유지한다.

```text
Routine
↓
RoutineRepeatDay
```

JPA 구현에서는 다음 두 방식 중 별도 Entity 방식을 기본으로 한다.

```text
1. 별도 Entity

RoutineRepeatDay

2. @ElementCollection
```

초기 구현에서는 관리와 제약조건 표현을 명확하게 하기 위해 별도 Entity를 사용한다.

---

# 7. DailyRoutine

특정 날짜에 사용자가 실제 수행할 루틴을 저장한다.

예:

```text
2026-08-18
07:00 운동
PENDING
```

## Table

```text
daily_routines
```

## Fields

| Column         | Type         | Nullable | Constraint | 설명              |
| -------------- | ------------ | -------: | ---------- | --------------- |
| id             | BIGINT       |       NO | PK         | DailyRoutine ID |
| user_id        | BIGINT       |       NO | FK         | 사용자             |
| routine_id     | BIGINT       |      YES | FK         | 원본 반복 Routine   |
| routine_date   | DATE         |       NO |            | 수행 날짜           |
| title          | VARCHAR(100) |       NO |            | 해당 날짜 루틴 제목     |
| scheduled_time | TIME         |       NO |            | 해당 날짜 수행 시간     |
| status         | VARCHAR(20)  |       NO |            | 수행 상태           |
| failure_reason | VARCHAR(500) |      YES |            | 실패 사유           |
| created_at     | DATETIME     |       NO |            | 생성 시간           |
| updated_at     | DATETIME     |       NO |            | 수정 시간           |

---

# 8. DailyRoutine Status

Enum:

```text
PENDING
SUCCESS
FAILED
```

의미:

| Status  | 설명              |
| ------- | --------------- |
| PENDING | 아직 수행하지 않음      |
| SUCCESS | 영상 인증을 통해 성공 처리 |
| FAILED  | 수행 실패           |

기본값:

```text
PENDING
```

---

# 9. failure_reason 정책

`failure_reason`은 FAILED 상태에서만 의미를 가진다.

예:

```text
status = FAILED
failure_reason = "야근으로 운동하지 못함"
```

SUCCESS 또는 PENDING 상태에서는 기본적으로:

```text
failure_reason = null
```

상태가 FAILED에서 다른 상태로 변경되면 실패 사유를 제거한다.

---

# 10. routine_id Nullable 이유

DailyRoutine에는 두 종류가 존재한다.

### 반복 루틴에서 생성

```text
Routine
↓
DailyRoutine
```

이 경우:

```text
routine_id != null
```

### 오늘만 추가

사용자가 특정 날짜에 일회성 루틴을 직접 추가할 수 있다.

이 경우 원본 Routine이 존재하지 않는다.

```text
routine_id = null
```

따라서 `routine_id`는 Nullable로 설계한다.

---

# 11. DailyRoutine Snapshot Policy

DailyRoutine에는 다음 값을 복사해서 저장한다.

```text
title
scheduled_time
```

이유:

원본 Routine이 이후 수정되어도 과거 기록은 변하지 않아야 한다.

예:

```text
8월 18일

Routine
07:00 운동

↓

DailyRoutine
07:00 운동
```

이후 Routine 수정:

```text
08:00 아침 공부
```

8월 18일 DailyRoutine은 그대로 유지한다.

```text
07:00 운동
```

즉 DailyRoutine은 특정 날짜 시점의 Snapshot 역할을 한다.

---

# 12. DailyRoutine Generation

특정 날짜를 조회할 때 반복 Routine을 기반으로 DailyRoutine을 생성한다.

예:

```text
Routine #10
월~금
07:00 운동

조회 날짜
2026-08-18 화요일

↓

DailyRoutine 생성
```

생성 데이터:

```text
user_id = 1
routine_id = 10
routine_date = 2026-08-18
title = 운동
scheduled_time = 07:00
status = PENDING
```

---

# 13. DailyRoutine Duplicate Prevention

같은 Routine이 같은 날짜에 중복 생성되면 안 된다.

따라서 반복 Routine 기반 DailyRoutine에는 다음 조합의 중복을 방지한다.

```text
routine_id + routine_date
```

단, `routine_id = null`인 일회성 DailyRoutine은 이 제약의 영향을 받지 않아야 한다.

Database 구현 시 MySQL NULL Unique 특성을 고려하고 Service에서도 중복 생성 여부를 검증한다.

---

# 14. DailyRoutine Sort

DailyRoutine 조회 기본 정렬:

```text
routine_date ASC
scheduled_time ASC
id ASC
```

특정 날짜 조회에서는:

```text
scheduled_time ASC
id ASC
```

예:

```text
07:00 기상
08:00 아침 운동
09:00 공부
22:00 독서
```

---

# 15. Video

Routine 성공 인증 영상을 저장한다.

실제 영상 Binary는 Database에 저장하지 않는다.

```text
Database
→ Metadata

S3
→ Actual Video
```

## Table

```text
videos
```

## Fields

| Column            | Type          | Nullable | Constraint | 설명              |
| ----------------- | ------------- | -------: | ---------- | --------------- |
| id                | BIGINT        |       NO | PK         | Video ID        |
| daily_routine_id  | BIGINT        |       NO | FK, UNIQUE | 연결 DailyRoutine |
| object_key        | VARCHAR(1000) |       NO | UNIQUE     | S3 Object Key   |
| original_filename | VARCHAR(255)  |      YES |            | 원본 파일명          |
| content_type      | VARCHAR(100)  |       NO |            | MIME Type       |
| duration_seconds  | DECIMAL(6,2)  |       NO |            | 영상 길이           |
| file_size         | BIGINT        |       NO |            | Byte 단위 파일 크기   |
| created_at        | DATETIME      |       NO |            | 생성 시간           |
| updated_at        | DATETIME      |       NO |            | 수정 시간           |

---

# 16. Video Constraints

영상 최대 길이:

```text
15 seconds
```

조건:

```text
duration_seconds > 0
duration_seconds <= 15
file_size > 0
```

정확한 최대 파일 크기는 실제 업로드 테스트 이후 결정한다.

Frontend와 Backend 모두에서 영상 길이를 검증한다.

---

# 17. DailyRoutine — Video Relationship

초기 MVP에서는 하나의 DailyRoutine에 성공 인증 영상 하나만 존재하도록 한다.

```text
DailyRoutine
1
│
0..1
Video
```

따라서:

```text
videos.daily_routine_id UNIQUE
```

를 적용한다.

---

# 18. Video Success Policy

영상 업로드 성공 후 DailyRoutine을 SUCCESS로 변경한다.

```text
S3 Upload
↓
Video Save
↓
DailyRoutine
PENDING → SUCCESS
```

영상 업로드가 실패한 경우:

```text
DailyRoutine
PENDING 유지
```

Video가 존재하지 않는데 SUCCESS가 되는 상황을 기본적으로 허용하지 않는다.

---

# 19. DailyVlog

사용자의 특정 날짜 성공 영상을 병합한 결과를 저장한다.

## Table

```text
daily_vlogs
```

## Fields

| Column           | Type          | Nullable | Constraint | 설명            |
| ---------------- | ------------- | -------: | ---------- | ------------- |
| id               | BIGINT        |       NO | PK         | DailyVlog ID  |
| user_id          | BIGINT        |       NO | FK         | 사용자           |
| vlog_date        | DATE          |       NO |            | 대상 날짜         |
| object_key       | VARCHAR(1000) |      YES | UNIQUE     | 생성된 영상 S3 Key |
| status           | VARCHAR(20)   |       NO |            | 생성 상태         |
| duration_seconds | DECIMAL(8,2)  |      YES |            | 완성 영상 길이      |
| failure_message  | VARCHAR(1000) |      YES |            | 생성 실패 사유      |
| created_at       | DATETIME      |       NO |            | 생성 시간         |
| updated_at       | DATETIME      |       NO |            | 수정 시간         |

---

# 20. DailyVlog Status

Enum:

```text
PROCESSING
SUCCESS
FAILED
```

흐름:

```text
생성 시작
↓
PROCESSING

FFmpeg 성공
↓
SUCCESS

FFmpeg 실패
↓
FAILED
```

---

# 21. DailyVlog Unique Constraint

한 사용자의 특정 날짜에는 하나의 DailyVlog Record만 존재하도록 한다.

```text
UNIQUE (user_id, vlog_date)
```

예:

```text
User #15
2026-08-18

→ DailyVlog 최대 1개
```

재생성 시 새로운 Row를 계속 생성하지 않고 기존 DailyVlog 상태를 갱신하는 방식을 기본으로 한다.

---

# 22. DailyVlog object_key

상태가 PROCESSING 또는 FAILED인 동안 실제 파일이 없을 수 있다.

따라서:

```text
object_key nullable
```

SUCCESS 상태에서는 반드시 Object Key가 존재해야 한다.

```text
status = SUCCESS
→ object_key != null
```

이 규칙은 Service에서 검증한다.

---

# 23. DailyVlog Failure

FFmpeg 또는 S3 업로드 실패 시:

```text
status = FAILED
```

필요한 경우:

```text
failure_message
```

에 실패 원인을 저장한다.

사용자에게 내부 Stack Trace나 민감정보를 그대로 노출하지 않는다.

---

# 24. BaseEntity

다음 Entity는 공통 시간 정보를 가진다.

```text
User
Routine
DailyRoutine
Video
DailyVlog
```

공통 구조:

```text
BaseEntity
├─ createdAt
└─ updatedAt
```

JPA에서는:

```text
@MappedSuperclass
```

기반으로 구현한다.

---

# 25. Primary Key Strategy

초기 MVP에서는 모든 Entity Primary Key를 다음 형태로 사용한다.

```text
BIGINT
AUTO_INCREMENT
```

JPA:

```text
GenerationType.IDENTITY
```

기준으로 구현한다.

---

# 26. Foreign Key Summary

```text
routines.user_id
→ users.id

routine_repeat_days.routine_id
→ routines.id

daily_routines.user_id
→ users.id

daily_routines.routine_id
→ routines.id

videos.daily_routine_id
→ daily_routines.id

daily_vlogs.user_id
→ users.id
```

---

# 27. Entity Ownership

모든 주요 Resource는 사용자 소유권을 가져야 한다.

```text
User
├─ Routine
├─ DailyRoutine
├─ Video
└─ DailyVlog
```

Video는 직접 `user_id`를 갖지 않는다.

소유권은 다음 관계를 통해 확인한다.

```text
Video
↓
DailyRoutine
↓
User
```

---

# 28. Routine Delete Policy

Routine 삭제 시 과거 DailyRoutine 기록은 삭제하지 않는다.

예:

```text
Routine 삭제
↓
과거 DailyRoutine 유지
```

따라서 DailyRoutine의 `routine_id`는 삭제된 Routine을 참조할 수 없으므로 삭제 정책을 고려해야 한다.

초기 정책:

```text
Routine 삭제
→ Soft Delete 또는 active=false 우선
```

MVP에서는 실제 Row 삭제보다:

```text
active = false
```

방식을 우선한다.

사용자 UI에서 "삭제"를 선택해도 내부적으로 비활성 처리하는 방식을 기본 정책으로 한다.

이유:

* 과거 DailyRoutine 참조 유지
* 기록 보존
* FK 문제 방지

---

# 29. User Delete Policy

MVP에서는 회원 탈퇴 기능을 구현하지 않는다.

향후 구현 시 다음 데이터가 연결된다.

```text
User
├─ Routine
├─ DailyRoutine
├─ Video
├─ DailyVlog
└─ S3 Object
```

따라서 단순 Cascade Delete 방식으로 구현하지 않는다.

회원 탈퇴 기능 도입 시 별도 데이터 삭제 정책을 설계한다.

---

# 30. Video Delete Policy

DailyRoutine 기록을 유지하더라도 Video를 삭제해야 하는 요구가 생길 수 있다.

삭제 시:

```text
Video DB
+
S3 Object
```

를 함께 처리해야 한다.

DB Row만 삭제하고 S3 파일을 남기는 Orphan Object를 만들지 않도록 한다.

---

# 31. DailyVlog Delete Policy

DailyVlog를 삭제하는 기능이 향후 추가되는 경우:

```text
DailyVlog DB
+
S3 Daily Vlog Object
```

를 함께 처리한다.

원본 Routine 인증 Video는 삭제하지 않는다.

---

# 32. Timestamp Policy

시간 컬럼:

```text
created_at
updated_at
```

루틴 날짜:

```text
routine_date
```

루틴 시간:

```text
scheduled_time
```

Daily Vlog 날짜:

```text
vlog_date
```

날짜와 시간 의미가 다른 값은 하나의 DATETIME으로 합치지 않는다.

---

# 33. Enum Storage

Enum은 문자열로 저장한다.

예:

```text
PENDING
SUCCESS
FAILED
```

JPA:

```java
@Enumerated(EnumType.STRING)
```

ORDINAL 방식은 사용하지 않는다.

금지:

```text
0
1
2
```

Enum 순서가 바뀔 경우 데이터 의미가 깨질 수 있기 때문이다.

---

# 34. Index Strategy

초기 예상 Index:

## User

```text
email
```

UNIQUE Index.

## Routine

```text
user_id
```

## RoutineRepeatDay

```text
routine_id
routine_id + day_of_week
```

## DailyRoutine

```text
user_id + routine_date
routine_id + routine_date
```

특정 날짜의 사용자 DailyRoutine 조회가 주요 Query이다.

## Video

```text
daily_routine_id
object_key
```

## DailyVlog

```text
user_id + vlog_date
```

실제 Query Plan과 데이터 증가를 확인한 뒤 필요한 Index만 추가한다.

---

# 35. Expected Core Queries

## 로그인

```sql
SELECT *
FROM users
WHERE email = ?
```

---

## 사용자 Routine 조회

```sql
SELECT *
FROM routines
WHERE user_id = ?
AND active = true
```

---

## 특정 요일 반복 Routine 조회

개념적으로:

```text
User Routine
+
RoutineRepeatDay
```

에서 해당 요일에 반복되는 Routine을 조회한다.

---

## 특정 날짜 DailyRoutine

```sql
SELECT *
FROM daily_routines
WHERE user_id = ?
AND routine_date = ?
ORDER BY scheduled_time ASC, id ASC
```

---

## 특정 날짜 성공 영상

```text
DailyRoutine
JOIN Video

WHERE
user_id = ?
routine_date = ?
status = SUCCESS

ORDER BY scheduled_time ASC
```

Daily Vlog 생성 시 사용한다.

---

## DailyVlog 조회

```sql
SELECT *
FROM daily_vlogs
WHERE user_id = ?
AND vlog_date = ?
```

---

# 36. ERD Diagram

```text
┌──────────────────┐
│       User       │
├──────────────────┤
│ id PK            │
│ email UNIQUE     │
│ password         │
│ name             │
│ profileImageKey  │
└───────┬──────────┘
        │
        │ 1
        │
        ├───────────────────────┐
        │                       │
        ▼ N                     ▼ N
┌──────────────────┐      ┌──────────────────┐
│     Routine      │      │    DailyVlog     │
├──────────────────┤      ├──────────────────┤
│ id PK            │      │ id PK            │
│ user_id FK       │      │ user_id FK       │
│ title            │      │ vlog_date        │
│ scheduled_time   │      │ object_key       │
│ active           │      │ status           │
└───────┬──────────┘      │ duration         │
        │                 └──────────────────┘
        │
        ├───────────────┐
        │               │
        ▼ N             ▼ N
┌──────────────────┐  ┌──────────────────────┐
│RoutineRepeatDay  │  │    DailyRoutine      │
├──────────────────┤  ├──────────────────────┤
│ id PK            │  │ id PK                │
│ routine_id FK    │  │ user_id FK           │
│ day_of_week      │  │ routine_id FK NULL   │
└──────────────────┘  │ routine_date         │
                      │ title                │
                      │ scheduled_time       │
                      │ status               │
                      │ failure_reason       │
                      └──────────┬───────────┘
                                 │
                                 │ 1
                                 │
                                 ▼ 0..1
                      ┌──────────────────────┐
                      │        Video         │
                      ├──────────────────────┤
                      │ id PK                │
                      │ daily_routine_id FK  │
                      │ object_key           │
                      │ duration_seconds     │
                      │ file_size            │
                      └──────────────────────┘
```

---

# 37. JPA Entity Mapping Summary

예상 Mapping:

## User

```text
User
@OneToMany
Routine

User
@OneToMany
DailyRoutine

User
@OneToMany
DailyVlog
```

양방향 관계가 실제로 필요하지 않은 경우 User에 Collection을 무조건 추가하지 않는다.

---

## Routine

```text
@ManyToOne
User

@OneToMany
RoutineRepeatDay
```

DailyRoutine은 Routine에서 Collection으로 탐색할 필요가 없다면 단방향 관계를 우선한다.

---

## DailyRoutine

```text
@ManyToOne
User

@ManyToOne(optional = true)
Routine
```

---

## Video

```text
@OneToOne
DailyRoutine
```

---

## DailyVlog

```text
@ManyToOne
User
```

---

# 38. JPA Relationship Principle

JPA 관계는 필요 최소한으로 구현한다.

다음과 같이 모든 관계를 무조건 양방향으로 만들지 않는다.

```text
User
↔ Routine
↔ DailyRoutine
↔ Video
```

기본적으로:

```text
Child → Parent
```

단방향 관계를 우선한다.

예:

```text
Routine → User
DailyRoutine → User
DailyRoutine → Routine
Video → DailyRoutine
DailyVlog → User
```

조회가 실제로 필요한 경우에만 역방향 Collection을 추가한다.

---

# 39. Cascade Policy

Cascade는 명시적인 필요가 있는 관계에만 적용한다.

후보:

```text
Routine
→ RoutineRepeatDay
```

RoutineRepeatDay는 Routine 없이 존재할 의미가 없기 때문에 다음을 검토할 수 있다.

```text
CascadeType.ALL
orphanRemoval = true
```

반면 다음 관계에는 무분별하게 Cascade Remove를 적용하지 않는다.

```text
User → DailyRoutine
Routine → DailyRoutine
DailyRoutine → Video
```

데이터 보존 정책을 우선 고려한다.

---

# 40. Validation Summary

## User

```text
email
- required
- valid format
- unique

password
- required

name
- required
- max 50
```

---

## Routine

```text
title
- required
- max 100

scheduledTime
- required

repeatDays
- 최소 1개

active
- required
```

---

## DailyRoutine

```text
routineDate
- required

title
- required
- max 100

scheduledTime
- required

status
- required
```

FAILED:

```text
failureReason
- optional
- max 500
```

---

## Video

```text
dailyRoutineId
- required
- unique

duration
- > 0
- <= 15

fileSize
- > 0

objectKey
- required
- unique
```

---

## DailyVlog

```text
userId + vlogDate
- unique

status
- required
```

SUCCESS:

```text
objectKey required
```

---

# 41. MVP Schema Summary

최종 MVP Table:

```text
users
routines
routine_repeat_days
daily_routines
videos
daily_vlogs
```

핵심 관계:

```text
User
↓
Routine
↓
DailyRoutine
↓
Video

User
↓
DailyVlog
```

반복요일:

```text
Routine
↓
RoutineRepeatDay
```

---

# 42. Design Principles

Database 설계에서는 다음 원칙을 따른다.

1. 반복 Template과 실제 수행 기록을 분리한다.
2. 과거 기록은 원본 Routine 변경에 영향을 받지 않는다.
3. 영상 Binary는 Database에 저장하지 않는다.
4. S3에는 파일을 저장하고 DB에는 Object Key를 저장한다.
5. 상태값은 Enum 문자열로 저장한다.
6. 사용자 Resource는 반드시 소유권을 확인할 수 있어야 한다.
7. 과거 기록을 보존하는 방향으로 삭제 정책을 설계한다.
8. Database Constraint와 Application Validation을 함께 사용한다.
9. 실제 Query 기준으로 Index를 설계한다.
10. 초기 MVP에 필요하지 않은 Entity를 미리 추가하지 않는다.
