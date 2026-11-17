# API 계약

담당자: 김서현

## 규칙

- 본문이 있는 성공 응답은 `global/response/ApiResponse`로 감싸 `success`와 `data`를 내려준다.
- 본문이 없는 성공 응답은 `204 No Content`로 응답하고 ApiResponse를 쓰지 않는다.
- 실패 응답은 `global/response/ErrorResponse`로 내려주고 HTTP 상태는 ErrorCode의 status를 따른다.
- 에러 코드는 `global/error/ErrorCode`에 정의한 값만 사용하고 `code`는 enum 이름과 같게 둔다.
- `message`는 사용자에게 그대로 보여줄 수 있는 한국어 문장으로 작성한다.
- `errors`는 필드 단위 검증 실패를 `field`, `reason`으로 담고 해당 없으면 빈 배열로 내려준다.
- FE는 HTTP 상태와 `code`로 분기하고 `message`는 화면 표시에만 사용한다.
- 응답 필드를 추가하거나 바꿀 때는 이 문서의 예시와 `ApiResponseTest`를 함께 수정한다.

## 하지 않는 것

- Entity를 `data`에 그대로 담지 않고 응답 DTO를 사용한다.
- 실패를 `200 OK`와 `success: false`로 내려주지 않는다.
- 예외 메시지, 스택 트레이스, SQL 같은 내부 정보를 `message`에 담지 않는다.
- ErrorCode에 없는 문자열 코드를 Controller나 Service에서 직접 만들지 않는다.

## 공통 에러 코드

| code | HTTP 상태 | message |
| --- | --- | --- |
| INVALID_INPUT | 400 | 입력값이 올바르지 않습니다. |
| UNAUTHORIZED | 401 | 인증이 필요합니다. |
| FORBIDDEN | 403 | 접근 권한이 없습니다. |
| NOT_FOUND | 404 | 요청한 리소스를 찾을 수 없습니다. |
| INTERNAL_ERROR | 500 | 서버 내부 오류가 발생했습니다. |

## 예시

아래 JSON은 `ApiResponseTest`가 실제 직렬화 결과와 비교한다.

### 성공 응답

`ApiResponse.ok(mapOf("videoId" to "dQw4w9WgXcQ", "commentCount" to 120))`

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

`ErrorResponse.of(ErrorCode.NOT_FOUND)`

```json
{
  "success": false,
  "code": "NOT_FOUND",
  "message": "요청한 리소스를 찾을 수 없습니다.",
  "errors": []
}
```

### 검증 실패 응답

`ErrorResponse.of(ErrorCode.INVALID_INPUT, listOf(FieldError("videoUrl", "유튜브 영상 URL 형식이 아닙니다.")))`

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
