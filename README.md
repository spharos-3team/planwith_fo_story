# Release Note / RM — planwith-fo-comment

## 1. 서비스 개요

---

`planwith-fo-comment`는 Story에 작성되는 댓글과 대댓글의 생성·조회·수정·삭제 및 운영 관리를 담당하는 MSA 서비스입니다.

주요 책임:

- Story 댓글 및 1단계 대댓글 관리
- 댓글 작성 가능 여부 검증
- 댓글 작성자·Story 작성자·관리자 권한 처리
- 좋아요·신고 이벤트 기반 Projection
- 신고 누적에 따른 댓글 자동 숨김
- 숨김 댓글 관리 화면 제공
- Member·Story 데이터의 로컬 Projection 유지
- 댓글 변경 이벤트의 Transactional Outbox 발행

기술 구성:

- Java 17
- Spring Boot 4.0.7
- Spring MVC
- Spring Data JPA
- MySQL
- Kafka
- Eureka Client
- Spring Boot Actuator
- Springdoc OpenAPI
- Gradle

현재 확인 기준:

- 브랜치: `develop`
- HEAD: `68121b8 Update README.md`
- 작업 트리: Clean

## 2. 도메인 범위

---

### Comment Aggregate

핵심 Aggregate는 `StoryComment`입니다.

관리 데이터:

- `commentUuid`
- `storyUuid`
- `memberUuid`
- `parentCommentUuid`
- `commentContent`
- `commentLikeCount`
- `reportCount`
- `moderationStatus`
- `hiddenAt`
- `createdAt`
- `updatedAt`
- `deletedAt`

### 댓글 규칙

- 댓글 내용은 필수입니다.
- 댓글 내용은 최대 1,000자입니다.
- 신규 댓글의 기본 상태는 `VISIBLE`입니다.
- 댓글 수정은 작성자만 가능합니다.
- 댓글 삭제는 Soft Delete 방식입니다.
- 삭제된 댓글은 다시 수정하거나 삭제할 수 없습니다.
- 삭제된 부모 댓글에 활성 대댓글이 있으면 부모 위치를 유지하고 삭제 안내 문구를 노출합니다.

### 대댓글 규칙

- 일반 댓글 아래에 1단계 대댓글을 작성할 수 있습니다.
- 대댓글 아래에 다시 대댓글을 작성할 수 없습니다.
- 삭제되거나 숨김 처리된 댓글에는 대댓글을 작성할 수 없습니다.
- 부모 댓글과 대댓글의 `storyUuid`가 동일해야 합니다.
- 대댓글은 작성 시간 오름차순으로 정렬됩니다.

### 삭제 권한

다음 사용자에게 댓글 삭제 권한이 있습니다.

- 댓글 작성자
- Story 작성자
- `ADMIN`

일반 회원은 타인의 댓글을 삭제할 수 없습니다.

### 댓글 작성 조건

댓글 작성 시 다음 조건을 검증합니다.

- 로그인 회원 여부
- Story Projection 존재 여부
- Story 상태가 `ACTIVE`인지 여부
- Story의 `commentEnabled`가 `true`인지 여부

삭제되었거나 댓글 작성이 비활성화된 Story에는 댓글을 작성할 수 없습니다.

### 댓글 정렬

지원 정렬:

- `LATEST`: 최신 댓글 우선
- `LIKE`: 좋아요 수 내림차순, 동률이면 최신순

정렬은 최상위 댓글에 적용하며 대댓글은 작성 시간순으로 배치합니다.

### Moderation

지원 상태:

- `VISIBLE`
- `HIDDEN`

신고 처리 규칙:

- Report Service의 신고 이벤트를 수신합니다.
- 신고 건수를 `reportCount` Projection에 반영합니다.
- 신고가 3건 이상 누적되면 댓글을 자동으로 `HIDDEN` 처리합니다.
- 숨김 시각을 `hiddenAt`에 저장합니다.
- 숨김 댓글은 일반 댓글 목록에서 제외됩니다.

### 관리 화면

Story 작성자 또는 `ADMIN`만 숨김 댓글 관리 화면에 접근할 수 있습니다.

관리 조회 조건:

- `moderationStatus = HIDDEN`
- `deletedAt IS NULL`

정렬:

- `reportCount DESC`
- 동일 신고 수에서는 `createdAt DESC`

관리 응답:

- 프로필 이미지
- 닉네임
- 댓글 내용
- 신고 횟수
- 작성일
- 숨김 처리일

### Projection

서비스 간 동기 호출을 줄이기 위해 로컬 Projection을 관리합니다.

- `CommentMemberProjection`
  - 닉네임
  - 프로필 이미지
  - 회원 상태
  - Source Version
- `CommentStoryProjection`
  - Story 작성자 UUID
  - 댓글 허용 여부
  - Story 상태
  - Source Version
- `CommentLikeProjection`
  - Like UUID
  - Comment UUID
  - 회원 UUID
- `CommentReportProjection`
  - Report UUID
  - Comment UUID
- `ProcessedCommentEvent`
  - 처리한 이벤트 UUID
  - 이벤트 타입
  - 대상 타입 및 UUID
  - 이벤트 발생 시각

## 3. API 그룹

---

### Comment Command API

기본 경로:

`/api/planwith-fo-comment/comments`

| Method | URL | 설명 | 인증 |
|---|---|---|---|
| `POST` | `/api/planwith-fo-comment/comments` | 댓글 또는 대댓글 작성 | 회원 필수 |
| `PATCH` | `/api/planwith-fo-comment/comments/{commentUuid}` | 댓글 내용 수정 | 작성자 |
| `DELETE` | `/api/planwith-fo-comment/comments/{commentUuid}` | 댓글 Soft Delete | 댓글 작성자, Story 작성자 또는 ADMIN |

댓글 작성 요청:

- `storyUuid`
- `commentContent`
- `parentCommentUuid`: 대댓글이 아닌 경우 `null`

댓글 수정 요청:

- `commentContent`

### Comment Query API

기본 경로:

`/api/planwith-fo-comment`

| Method | URL | 설명 | 인증 |
|---|---|---|---|
| `GET` | `/api/planwith-fo-comment/stories/{storyUuid}/comments` | Story 댓글과 대댓글 목록 조회 | 비회원 가능 |
| `GET` | `/api/planwith-fo-comment/comments/{commentUuid}` | 댓글 상세 조회 | 비회원 가능 |
| `GET` | `/api/planwith-fo-comment/stories/{storyUuid}/comments/management` | 숨김 댓글 관리 조회 | Story 작성자 또는 ADMIN |

목록 정렬 파라미터:

- `sort=LATEST`
- `sort=LIKE`

### 권한 헤더

- `X-Member-Uuid`
- `X-Member-Role`

`X-Member-Role` 지원값:

- `USER`
- `ADMIN`

알 수 없는 Role 값은 `USER`로 처리합니다.

### 응답 권한 정보

댓글 응답에는 현재 조회자를 기준으로 다음 권한이 포함됩니다.

- `canEdit`
- `canDelete`

## 4. 외부 연동

---

### Member Service

수신 이벤트:

- `member.changed`

Projection 반영 데이터:

- 회원 UUID
- 닉네임
- 프로필 이미지
- 회원 상태
- Source Version

회원 정보를 댓글 조회 시 외부 API로 매번 호출하지 않고 로컬 Projection에서 조회합니다.

### Story Service

수신 이벤트:

- `story.created`
- `story.updated`
- `story.deleted`

Projection 반영 데이터:

- Story UUID
- Story 작성자 UUID
- 댓글 허용 여부
- Story 상태
- Source Version

활용 범위:

- 댓글 작성 가능 여부
- Story 삭제 여부
- Story 작성자의 댓글 삭제 권한
- 숨김 댓글 관리 권한

### Like Service

수신 이벤트:

- `like.created`
- `like.removed`

처리 내용:

- Comment 대상 이벤트만 처리
- `commentLikeCount` 증감
- Like UUID Projection 저장
- 중복 Like 생성 방지
- 이벤트 UUID 기반 중복 처리 방지
- 이벤트 발생 시각 기반 역순 이벤트 방지
- 좋아요 수가 0 미만으로 내려가지 않도록 보호

### Report Service

수신 이벤트:

- `report.created`

처리 내용:

- Comment 대상 신고 반영
- `reportCount` 증가
- Report UUID Projection 저장
- 동일 신고 및 동일 이벤트 중복 반영 방지
- 신고 3건 이상 누적 시 댓글 자동 숨김

Report Service가 소유하는 신고 사유 등은 Comment Service에 원본 데이터로 저장하지 않습니다.

### Comment 이벤트 발행

발행 이벤트:

- `COMMENT_CREATED` → `comment.created`
- `COMMENT_UPDATED` → `comment.updated`
- `COMMENT_DELETED` → `comment.deleted`

이벤트 Payload:

- 이벤트 타입
- 댓글 UUID
- Story UUID
- 회원 UUID
- 부모 댓글 UUID
- 변경 시각

발행 구조:

`Comment 트랜잭션 → Comment Outbox 저장 → Scheduler 조회 → Kafka 발행 → PUBLISHED 처리`

## 5. 비기능 / 품질

---

### 아키텍처

Hexagonal Architecture 형태로 구성되어 있습니다.

- Web·Kafka Consumer: Inbound Adapter
- Application Use Case: Port In
- JPA·Kafka Publisher: Port Out Adapter
- Domain: 댓글 규칙 및 상태 변경
- Service: 트랜잭션과 Use Case 조합

### 데이터 정합성

- 댓글 저장과 Outbox 저장을 동일 트랜잭션에서 처리
- 댓글 Like·Report 처리 시 비관적 쓰기 잠금 사용
- 이벤트 UUID 기반 중복 처리 방지
- 이벤트 발생 시각과 Source Version 기반 역순·구버전 이벤트 방지
- Like UUID와 Report UUID에 대한 별도 Projection 관리
- Soft Delete 적용
- 좋아요 Counter 음수 방지

### 조회 성능

다음 인덱스가 Entity에 정의되어 있습니다.

- Story별 활성 댓글과 작성일
- 회원별 활성 댓글
- Story·부모 댓글·작성일
- Story·Moderation 상태·신고 횟수
- Like Projection의 Comment UUID
- Report Projection의 Comment UUID
- Outbox 상태와 생성 시각
- 처리 이벤트의 대상과 발생 시각

Member·Story 정보는 로컬 Projection을 사용하므로 댓글 목록마다 외부 서비스를 반복 호출하지 않습니다.

### 예외 처리

주요 도메인 예외:

- 로그인 필요
- 댓글을 찾을 수 없음
- Story를 찾을 수 없음
- 댓글 작성 불가
- 삭제된 Story
- 댓글 작성자 불일치
- 댓글 삭제 권한 없음
- 댓글 관리 권한 없음
- 이미 삭제된 댓글
- 유효하지 않은 댓글 내용
- 허용되지 않는 중첩 대댓글

전역 예외 처리기를 통해 API 오류 응답으로 변환합니다.

### 현재 캐시 상태

`CommentCachePort` 경계는 존재하지만 실제 Redis Adapter는 구현되어 있지 않습니다. 현재 조회는 JPA와 로컬 Projection을 기준으로 동작합니다.

## 6. 배포 설정 요약

---

### 애플리케이션

- 서비스명: `planwith-fo-comment`
- 기본 Server Port: 동적 할당 `0`
- Docker Port: `8090`
- Java Runtime: Eclipse Temurin 17 JRE Alpine
- Eureka 기본 활성화
- Actuator Health·Info 노출
- OpenAPI 경로: `/v3/api-docs`
- Swagger UI 경로: `/swagger-ui.html`

### Docker

- Gradle 기반 Multi-stage Build
- 실행 이미지에서 비root `spring` 사용자 사용
- 컨테이너명: `planwith-planwith-fo-comment`
- Docker Network: `planwith-net`
- 로컬 포트 바인딩: `127.0.0.1:8090:8090`

### Gateway

Gateway Route:

- `/api/planwith-fo-comment/**`
- `/docs/planwith-fo-comment/**`

Eureka URI:

- `lb://planwith-fo-comment`

### 필수 데이터베이스 환경변수

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JPA_DDL_AUTO`

로컬 예시:

- MySQL Host Port: `3307`
- Database: `comment_db`
- 기본 Schema 관리: `JPA_DDL_AUTO=update`

### Kafka 환경변수

기능 활성화:

- `KAFKA_ENABLED`
- `KAFKA_LISTENER_ENABLED`
- `OUTBOX_PUBLISHER_ENABLED`
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_CONSUMER_GROUP`

Topic 설정:

- `KAFKA_TOPIC_MEMBER_CHANGED`
- `KAFKA_TOPIC_STORY_CREATED`
- `KAFKA_TOPIC_STORY_UPDATED`
- `KAFKA_TOPIC_STORY_DELETED`
- `KAFKA_TOPIC_LIKE_CREATED`
- `KAFKA_TOPIC_LIKE_REMOVED`
- `KAFKA_TOPIC_REPORT_CREATED`
- `KAFKA_TOPIC_COMMENT_CREATED`
- `KAFKA_TOPIC_COMMENT_UPDATED`
- `KAFKA_TOPIC_COMMENT_DELETED`

Outbox 설정:

- `OUTBOX_PUBLISHER_ENABLED`
- `OUTBOX_POLL_INTERVAL_MS`

### Eureka·Gateway 환경변수

- `EUREKA_CLIENT_ENABLED`
- `EUREKA_DEFAULT_ZONE`
- `EUREKA_PREFER_IP_ADDRESS`
- `GATEWAY_PUBLIC_URL`
- `SPRINGDOC_SWAGGER_UI_ENABLED`

## 7. 운영 주의사항

---

### Kafka 기능 기본 비활성화

기본 설정은 다음과 같습니다.

- `KAFKA_ENABLED=false`
- `KAFKA_LISTENER_ENABLED=false`
- `OUTBOX_PUBLISHER_ENABLED=false`

운영 배포 시 세 설정을 활성화하지 않으면:

- Member·Story Projection이 갱신되지 않음
- Like·Report 이벤트가 반영되지 않음
- Comment 생성·수정·삭제 이벤트가 Kafka로 발행되지 않음

### 인증 헤더 보호

서비스는 `X-Member-Uuid`, `X-Member-Role` 헤더를 신뢰합니다.

서비스 포트를 외부에 직접 노출하면 사용자가 `ADMIN` 헤더를 위조할 수 있으므로 다음 조건이 필요합니다.

- 외부 요청은 Gateway를 통해서만 허용
- Gateway가 사용자 입력 인증 헤더를 제거
- 인증 완료 후 Gateway가 검증된 회원 UUID와 Role을 새로 주입
- 내부 서비스 포트는 외부 네트워크에서 접근 차단

### Outbox Kafka ACK 처리

현재 Kafka Publisher는 `KafkaTemplate.send()`의 완료 ACK를 기다리지 않고 반환하며, 이후 Outbox를 `PUBLISHED`로 변경합니다.

운영 신뢰성을 높이려면 다음 보완을 권장합니다.

- Kafka 전송 Future 완료 확인 후 `PUBLISHED` 처리
- 발행 실패 시 `PENDING` 유지
- 재시도 횟수와 다음 재시도 시각 관리
- Dead Letter 또는 운영 알림 정책 적용

### Schema 관리

현재 기본값은 `hibernate.ddl-auto=update`이며 별도 Flyway·Liquibase Migration은 확인되지 않았습니다.

운영 환경에서는 다음을 권장합니다.

- 명시적 DB Migration 도입
- 운영 환경에서 `ddl-auto=validate` 사용
- 배포 전 인덱스와 제약조건 검증

### Soft Delete

댓글 삭제는 실제 행 삭제가 아닙니다.

- 삭제 댓글은 `deletedAt`으로 관리
- 활성 대댓글이 있는 삭제 부모는 목록 구조 유지를 위해 조회
- 삭제 부모 내용은 삭제 안내 문구로 대체
- 보관 기간과 물리 삭제 정책은 별도로 정의 필요

### Projection 선행 조건

Story Projection이 없으면 댓글을 작성할 수 없습니다. 초기 배포 또는 Kafka 장애 이후에는 다음을 확인해야 합니다.

- 기존 Story·Member 데이터 Backfill
- Consumer Lag
- Projection 동기화 완료 여부
- Story·Member Source Version 정합성

### 테스트 환경 차이

자동화 테스트는 H2의 MySQL 호환 모드를 사용합니다. 실제 MySQL 및 실제 Kafka Broker를 연결한 Testcontainers/E2E 테스트는 현재 테스트 구성에서 확인되지 않았습니다.

## 8. 개발 완료 범위 (단계 요약)

---

### STEP 01. Comment Domain

- StoryComment Aggregate
- ModerationStatus
- CommentSort
- CommentEventType
- 댓글 내용 Validation
- Soft Delete

### STEP 02. 댓글 생성

- 로그인 검증
- Story Projection 검증
- 댓글 허용 여부 검증
- 댓글 생성 후 즉시 응답
- CommentCreated Outbox 저장

### STEP 03. 대댓글

- 1단계 대댓글 생성
- 부모 댓글과 Story 일치 검증
- 중첩 대댓글 차단
- 삭제·숨김 부모에 대한 대댓글 차단

### STEP 04. 댓글 조회

- Story별 댓글 목록
- 댓글 상세
- 비회원 조회
- 최신순·좋아요순 정렬
- 대댓글 Thread 조립
- 조회자별 수정·삭제 권한 응답

### STEP 05. 댓글 수정·삭제

- 작성자 수정
- 댓글 작성자 삭제
- Story 작성자 삭제
- ADMIN 삭제
- Soft Delete
- 활성 대댓글이 있는 삭제 부모 유지

### STEP 06. Member·Story Projection

- MemberChanged 동기화
- StoryCreated·StoryUpdated 동기화
- StoryDeleted 반영
- Source Version 기반 구버전 이벤트 차단
- Projection 기반 댓글 작성·권한 검증

### STEP 07. Like Projection

- CommentLiked 반영
- CommentUnliked 반영
- Like UUID 기반 중복 방지
- 이벤트 UUID 기반 멱등 처리
- 역순 이벤트 차단
- 음수 Counter 방지

### STEP 08. Report·Moderation

- CommentReported 반영
- Report UUID 기반 중복 방지
- 신고 Counter 증가
- 신고 3회 자동 숨김
- 숨김 시각 저장

### STEP 09. 댓글 관리

- Story 작성자·ADMIN 관리 권한
- 숨김 댓글 조회
- 신고 횟수 내림차순 정렬
- 관리 화면에서 댓글 삭제

### STEP 10. Outbox·Kafka

- 생성·수정·삭제 Outbox 저장
- Pending Outbox Polling
- Kafka Topic 분기 발행
- 발행 완료 상태 관리
- Kafka 비활성화 시 Logging Publisher 사용

### STEP 11. 배포·문서화

- Docker Multi-stage Build
- 비root 컨테이너 실행
- Eureka 등록
- Gateway Route
- OpenAPI 문서
- Actuator Health Check

## 9. 검증 상태

---

전체 테스트를 강제 재실행했습니다.

실행 명령:

`.\gradlew.bat test --rerun-tasks --console=plain --quiet`

결과:

| 항목 | 결과 |
|---|---:|
| 전체 테스트 | 62 |
| 성공 | 62 |
| 실패 | 0 |
| 오류 | 0 |
| 스킵 | 0 |

검증된 주요 시나리오:

- 댓글 생성·목록·상세·수정·삭제 Web API
- 댓글 생성 즉시 응답
- 비회원 작성 차단 및 조회 허용
- Story 미존재·댓글 비활성 Story 작성 차단
- 댓글 작성자만 수정 가능
- 댓글 작성자·Story 작성자·ADMIN 삭제
- 대댓글 생성 및 중첩 대댓글 차단
- 삭제 부모와 활성 대댓글 유지
- 최신순·좋아요순 정렬
- 조회자별 `canEdit`, `canDelete`
- Like 생성·삭제 Counter 반영
- Like 중복·역순 이벤트 차단
- Report 중복 이벤트 차단
- 신고 Counter와 자동 숨김
- 숨김 댓글 관리 권한 및 조회
- Member·Story Projection 구버전 이벤트 차단
- 댓글과 Outbox 동일 트랜잭션 저장
- 전체 Comment 이벤트 스토밍 시나리오

검증 한계:

- 실제 MySQL 연결 테스트 없음
- 실제 Kafka Broker 연결 테스트 없음
- 실제 Gateway 인증 헤더 주입 E2E 테스트 없음
- Outbox 발행의 Kafka ACK 완료 보장 테스트 없음

---

**RM 결론:**

`planwith-fo-comment`의 댓글·대댓글·수정·Soft Delete·권한·Like·Report·자동 숨김·관리 조회·Member/Story Projection·Outbox 기능은 개발 완료 상태이며 전체 62개 자동화 테스트가 통과했습니다.

기능 기준으로 Release Candidate로 판단할 수 있습니다. 다만 운영 배포 전 Kafka Consumer·Publisher·Outbox 활성화, Gateway 인증 헤더 보호, 실제 MySQL/Kafka 통합 검증, Kafka ACK 확인 후 Outbox 완료 처리 보완이 필요합니다.
