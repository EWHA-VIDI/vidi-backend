# API 계약

담당자: 김서현

## 규칙

- 본문이 있는 성공 응답은 `global/response/ApiResponse`로 감싸 `success`와 `data`를 내려준다.
- 본문이 없는 성공 응답은 `204 No Content`로 응답하고 ApiResponse를 쓰지 않는다.
- 실패 응답은 `global/response/ErrorResponse`로 내려주고 HTTP 상태는 ErrorCode의 status를 따른다.
- 에러 코드는 `global/error/ErrorCode`에 정의한 값만 사용하고, 응답의 `code`는 enum 이름이다.
- 요청 형식 검증 실패는 `INVALID_INPUT`과 `errors`로 내려주고, FE가 분기해야 하는 비즈니스 규칙 위반은 도메인 ErrorCode를 추가한다.
- `message`는 사용자에게 그대로 보여줄 수 있는 한국어 문장으로 작성하고 예외 메시지, 스택 트레이스, SQL 같은 내부 정보를 담지 않는다.
- `errors`는 필드 단위 검증 실패를 `field`, `reason`으로 담고 해당 없으면 빈 배열로 내려준다.
- FE는 HTTP 상태와 `code`로 분기하고 `message`는 화면 표시에만 사용한다.
- 목록 응답은 Spring Data `Page`를 `PageResponse.from(page) { 응답 DTO 변환 }`으로 바꿔 `ApiResponse`의 `data`에 담는다.
- `page`는 0부터 시작하고 `hasNext`로 다음 페이지 여부를 알려준다.
- 비즈니스 실패는 `BusinessException(ErrorCode.X)`를 던지고, Controller에서 잡아 ErrorResponse를 직접 만들지 않는다.

## 예시

### 성공 응답

```json
{
  "success": true,
  "data": {
    "videoId": "dQw4w9WgXcQ",
    "commentCount": 120
  }
}
```

### 실패 응답

```json
{
  "success": false,
  "code": "NOT_FOUND",
  "message": "요청한 리소스를 찾을 수 없습니다.",
  "errors": []
}
```

### 검증 실패 응답

```json
{
  "success": false,
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다.",
  "errors": [
    {
      "field": "videoUrl",
      "reason": "유튜브 영상 URL 형식이 아닙니다."
    }
  ]
}
```

### 목록 응답

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "commentId": 1,
        "authorName": "viewer1"
      },
      {
        "commentId": 2,
        "authorName": "viewer2"
      }
    ],
    "page": 0,
    "size": 2,
    "totalElements": 5,
    "totalPages": 3,
    "hasNext": true
  }
}
```
