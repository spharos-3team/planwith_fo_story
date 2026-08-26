# planwith_fo_story 기능별 Swagger 테스트 계획표

공통 테스트 값

```text
AUTHOR_UUID: 11111111-1111-1111-1111-111111111111
VIEWER_UUID: 22222222-2222-2222-2222-222222222222
OTHER_UUID: 33333333-3333-3333-3333-333333333333
STORY_UUID: 스토리 생성 응답의 storyUuid 값
```

인증이 필요한 API는 Member 서비스의 `POST /api/v1/auth/login` 응답에서 사용자별 `accessToken`을 발급받아 사용한다.
Gateway Swagger UI의 **Authorize**에서 `Bearer`를 선택하고 토큰 값만 입력한다. Swagger UI는 요청에
`Authorization: Bearer <accessToken>`을 추가하며, Gateway는 검증된 토큰의 `sub`를
`X-Auth-User-Id` 헤더로 변환하여 Story 서비스에 전달한다. `X-MEMBER-UUID` 또는
`X-Auth-User-Id`를 클라이언트에서 직접 입력하지 않는다. 인증 없음 시나리오는 Authorize에서
Logout한 뒤 실행한다.

응답 예시는 기능별 검증에 필요한 핵심 필드를 중심으로 작성했다. 실제 Swagger 응답에는 DTO에 정의된 생성·수정 일시, 국가·도시·장소 등 추가 필드가 함께 표시될 수 있다.

---

인증: 없음

기능명: 배포 상태 확인
api 명: GET /api/planwith-fo-story/deploy-check

Request body:
```text
없음
```

Response body:
```json
{
  "service": "planwith-fo-story",
  "marker": "planwith-fo-story-deploy-v1",
  "message": "planwith-fo-story deploy pipeline ok"
}
```

---

인증: 없음

기능명: 로그인
api 명: POST /api/planwith-fo-story/login

Request body:
```json
{
  "id": "test-001",
  "pw": "1234"
}
```

Response body:
```json
{
  "id": "test-001",
  "message": "로그인에 성공했습니다."
}
```

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 생성
api 명: POST /api/stories

Request body:
```json
{
  "title": "서울 여행 첫 번째 스토리",
  "content": "Swagger 기능 테스트용 스토리입니다.",
  "coverImageUrl": "https://images.example.com/story-cover.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "aiVerificationRequested": false,
  "countries": [
    {
      "countryName": "대한민국",
      "displayOrder": 0,
      "cities": [
        {
          "cityName": "서울",
          "displayOrder": 0,
          "places": [
            {
              "placeName": "경복궁",
              "displayOrder": 0,
              "images": [
                {
                  "imageUrl": "https://images.example.com/gyeongbokgung.jpg",
                  "imageOrder": 1
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  "tags": ["서울", "궁궐"],
  "visibilityMemberUuids": []
}
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "title": "서울 여행 첫 번째 스토리",
  "content": "Swagger 기능 테스트용 스토리입니다.",
  "coverImageUrl": "https://images.example.com/story-cover.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "aiModerationStatus": "UNVERIFIED",
  "viewCount": 0,
  "storyLikeCount": 0,
  "storyCommentCount": 0,
  "tags": ["서울", "궁궐"],
  "visibilityMemberUuids": []
}
```

확인 사항: HTTP 201인지 확인하고 응답의 `storyUuid`를 이후 테스트의 `STORY_UUID`로 사용한다.

---

인증: 없음

기능명: 공개 스토리 목록 조회
api 명: GET /api/stories?page=0&size=20&sort=LATEST

Request body:
```text
없음
```

Response body:
```json
{
  "items": [
    {
      "storyUuid": "<STORY_UUID>",
      "coverImageUrl": "https://images.example.com/story-cover.jpg",
      "memberUuid": "11111111-1111-1111-1111-111111111111",
      "authorNickname": null,
      "title": "서울 여행 첫 번째 스토리",
      "countries": ["대한민국"],
      "cities": ["서울"],
      "createdAt": "<생성일시>",
      "storyLikeCount": 0,
      "storyCommentCount": 0,
      "viewCount": 0
    }
  ],
  "page": 0,
  "size": 20
}
```

---

인증: 없음

기능명: 공개 스토리 상세 조회
api 명: GET /api/stories/{STORY_UUID}

Request body:
```text
없음
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "coverImageUrl": "https://images.example.com/story-cover.jpg",
  "title": "서울 여행 첫 번째 스토리",
  "content": "Swagger 기능 테스트용 스토리입니다.",
  "countries": [
    {
      "countryName": "대한민국",
      "displayOrder": 0,
      "cities": []
    }
  ],
  "places": [],
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "tags": ["서울", "궁궐"],
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "aiModerationStatus": "UNVERIFIED",
  "viewCount": 0,
  "storyLikeCount": 0,
  "storyCommentCount": 0,
  "createdAt": "<생성일시>",
  "updatedAt": "<수정일시>"
}
```

---

인증: 없음

기능명: 국가명 스토리 검색
api 명: GET /api/stories/search?type=COUNTRY&keyword=대한민국&page=0&size=20

Request body:
```text
없음
```

Response body:
```json
{
  "items": [
    {
      "storyUuid": "<STORY_UUID>",
      "title": "서울 여행 첫 번째 스토리",
      "countries": ["대한민국"],
      "cities": ["서울"]
    }
  ],
  "page": 0,
  "size": 20
}
```

---

인증: 없음

기능명: 도시명 스토리 검색
api 명: GET /api/stories/search?type=CITY&keyword=서울&page=0&size=20

Request body:
```text
없음
```

Response body:
```json
{
  "items": [
    {
      "storyUuid": "<STORY_UUID>",
      "title": "서울 여행 첫 번째 스토리",
      "countries": ["대한민국"],
      "cities": ["서울"]
    }
  ],
  "page": 0,
  "size": 20
}
```

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 내 스토리 목록 조회
api 명: GET /api/stories/me?country=대한민국&city=서울&page=0&size=20

Request body:
```text
없음
```

Response body:
```json
{
  "items": [
    {
      "storyUuid": "<STORY_UUID>",
      "coverImageUrl": "https://images.example.com/story-cover.jpg",
      "title": "서울 여행 첫 번째 스토리",
      "countries": ["대한민국"],
      "cities": ["서울"],
      "createdAt": "<생성일시>",
      "storyLikeCount": 0,
      "storyCommentCount": 0,
      "viewCount": 0
    }
  ],
  "page": 0,
  "size": 20
}
```

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 내 스토리 상세 조회
api 명: GET /api/stories/me/{STORY_UUID}

Request body:
```text
없음
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "title": "서울 여행 첫 번째 스토리",
  "content": "Swagger 기능 테스트용 스토리입니다.",
  "visibilityScope": "ALL",
  "commentEnabled": true,
  "viewCount": 0,
  "storyLikeCount": 0,
  "storyCommentCount": 0
}
```

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 피드 조회
api 명: GET /api/story-feeds?page=0&size=20&sort=LATEST&feedType=FOLLOWING

Request body:
```text
없음
```

Response body:
```json
{
  "items": [
    {
      "storyUuid": "<STORY_UUID>",
      "title": "서울 여행 첫 번째 스토리",
      "countries": ["대한민국"],
      "cities": ["서울"]
    }
  ],
  "page": 0,
  "size": 20
}
```

확인 사항: local 프로필에서는 외부 회원 조회가 비활성화되어 공개 목록 fallback 결과를 확인한다.

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 상세 화면 통합 조회
api 명: GET /api/bff/stories/{STORY_UUID}

Request body:
```text
없음
```

Response body:
```json
{
  "story": {
    "storyUuid": "<STORY_UUID>",
    "memberUuid": "11111111-1111-1111-1111-111111111111",
    "title": "서울 여행 첫 번째 스토리",
    "commentEnabled": true,
    "visibilityScope": "ALL"
  },
  "member": {
    "memberUuid": "11111111-1111-1111-1111-111111111111",
    "nickname": null,
    "profileImageUrl": null,
    "bio": null
  },
  "follow": {
    "followerCount": 0,
    "followingCount": 0
  },
  "schedule": null,
  "like": {
    "liked": false,
    "storyLikeCount": 0
  },
  "comment": {
    "uiState": "COMMENT_UI",
    "message": null,
    "items": []
  },
  "membership": {
    "subscribed": false
  }
}
```

---

인증: 없음

기능명: 스토리 조회수 증가
api 명: POST /api/stories/{STORY_UUID}/views

Request body:
```text
없음
```

Response body:
```text
없음 (HTTP 204)
```

확인 사항: 상세 조회 API를 다시 실행하여 `viewCount`가 1 증가했는지 확인한다.

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 수정
api 명: PATCH /api/stories/{STORY_UUID}

Request body:
```json
{
  "title": "서울 여행 수정 스토리",
  "content": "작성자가 수정한 본문입니다.",
  "coverImageUrl": "https://images.example.com/story-cover-updated.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-06",
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "aiVerificationRequested": false,
  "countries": [
    {
      "countryName": "대한민국",
      "displayOrder": 0,
      "cities": [
        {
          "cityName": "서울",
          "displayOrder": 0,
          "places": []
        }
      ]
    }
  ],
  "tags": ["서울", "수정"],
  "visibilityMemberUuids": []
}
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "title": "서울 여행 수정 스토리",
  "content": "작성자가 수정한 본문입니다.",
  "coverImageUrl": "https://images.example.com/story-cover-updated.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-06",
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "tags": ["서울", "수정"]
}
```

확인 사항: 응답에는 다른 상세 필드도 포함되며 위 변경 필드가 요청값과 일치하는지 확인한다.

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 댓글 허용 여부 변경
api 명: PATCH /api/stories/{STORY_UUID}/comment-enabled

Request body:
```json
{
  "commentEnabled": false
}
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "commentEnabled": false,
  "visibilityScope": "ALL"
}
```

확인 사항: 응답에는 다른 상세 필드도 포함되며 `commentEnabled=false`인지 확인한다.

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 공개범위 변경
api 명: PATCH /api/stories/{STORY_UUID}/visibility

Request body:
```json
{
  "visibilityScope": "MEMBER"
}
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "commentEnabled": false,
  "visibilityScope": "MEMBER"
}
```

확인 사항: Logout 상태로 상세 조회하면 HTTP 403, `OTHER_UUID` 사용자의 Bearer 토큰으로 조회하면 HTTP 200인지 확인한다.

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: PRIVATE 스토리 및 지정 회원 설정
api 명: PATCH /api/stories/{STORY_UUID}

Request body:
```json
{
  "title": "서울 여행 비공개 스토리",
  "content": "지정 회원만 조회할 수 있습니다.",
  "coverImageUrl": "https://images.example.com/story-cover-private.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-06",
  "commentEnabled": false,
  "visibilityScope": "PRIVATE",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "aiVerificationRequested": false,
  "countries": [
    {
      "countryName": "대한민국",
      "displayOrder": 0,
      "cities": [
        {
          "cityName": "서울",
          "displayOrder": 0,
          "places": []
        }
      ]
    }
  ],
  "tags": ["서울", "비공개"],
  "visibilityMemberUuids": [
    "22222222-2222-2222-2222-222222222222"
  ]
}
```

Response body:
```json
{
  "storyUuid": "<STORY_UUID>",
  "memberUuid": "11111111-1111-1111-1111-111111111111",
  "title": "서울 여행 비공개 스토리",
  "visibilityScope": "PRIVATE",
  "visibilityMemberUuids": [
    "22222222-2222-2222-2222-222222222222"
  ]
}
```

확인 사항: `VIEWER_UUID` 사용자의 Bearer 토큰으로 조회하면 HTTP 200, `OTHER_UUID` 사용자의 Bearer 토큰 또는 Logout 상태로 조회하면 HTTP 403인지 확인한다.

---

인증: 필요 (OTHER_UUID 사용자의 Bearer 토큰)

기능명: 타 회원 스토리 수정 권한 실패
api 명: PATCH /api/stories/{STORY_UUID}

Request body:
```json
{
  "title": "권한 없는 수정",
  "content": "수정되면 안 되는 본문입니다.",
  "coverImageUrl": "https://images.example.com/denied.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-06",
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "aiVerificationRequested": false,
  "countries": [
    {
      "countryName": "대한민국",
      "displayOrder": 0,
      "cities": [
        {
          "cityName": "서울",
          "displayOrder": 0,
          "places": []
        }
      ]
    }
  ],
  "tags": [],
  "visibilityMemberUuids": []
}
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 403,
  "code": "STORY_ACCESS_DENIED",
  "message": "<접근 거부 메시지>"
}
```

---

인증: 없음 (401 응답 확인)

기능명: 스토리 생성 인증 실패
api 명: POST /api/stories

Request body:
```json
{
  "title": "인증 실패 테스트",
  "content": "인증 토큰이 없는 요청입니다.",
  "coverImageUrl": "https://images.example.com/unauthorized.jpg",
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "commentEnabled": true,
  "visibilityScope": "ALL",
  "scheduleUuid": null,
  "scheduleVisible": false,
  "aiVerificationRequested": false,
  "countries": [
    {
      "countryName": "대한민국",
      "displayOrder": 0,
      "cities": [
        {
          "cityName": "서울",
          "displayOrder": 0,
          "places": []
        }
      ]
    }
  ],
  "tags": [],
  "visibilityMemberUuids": []
}
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 401,
  "code": "MEMBER_AUTHENTICATION_REQUIRED",
  "message": "<인증 필요 메시지>"
}
```

---

인증: 필요 (AUTHOR_UUID 사용자의 Bearer 토큰)

기능명: 스토리 삭제
api 명: DELETE /api/stories/{STORY_UUID}

Request body:
```text
없음
```

Response body:
```text
없음 (HTTP 204)
```

확인 사항: 삭제 후 상세 조회 결과가 HTTP 404이고 오류 코드가 `STORY_NOT_FOUND`인지 확인한다.

## 기능 실행 순서

1. 배포 상태 확인과 로그인
2. 스토리 생성 후 `STORY_UUID` 저장
3. 목록·상세·검색·내 스토리·피드·통합 상세 조회
4. 조회수 증가
5. 스토리 수정과 댓글 허용 여부 변경
6. MEMBER 공개범위 접근 확인
7. PRIVATE 지정 회원 접근 확인
8. 타 회원 권한 실패 확인
9. 스토리 삭제와 삭제 후 404 확인

## 로컬 단독 테스트 제한사항

- 회원·팔로우·멤버십·좋아요·댓글 외부 조회가 비활성화되어 관련 응답은 기본값 또는 fallback 결과다.
- 닉네임 검색은 회원 서비스 연동이 필요하므로 local 단독 테스트에서 제외한다.
- 좋아요·댓글 카운트는 Kafka 이벤트 연동 테스트에서 별도로 확인한다.
- 실제 AI 검증은 `STORY_OPENAI_ENABLED=true`와 유효한 `OPENAI_API_KEY`가 필요하다.
- 일정 소유권 확인은 Schedule 서비스 연동 테스트에서 별도로 확인한다.
