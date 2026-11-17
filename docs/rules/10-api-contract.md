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
- 인증·인가 실패도 Security의 EntryPoint와 AccessDeniedHandler가 같은 ErrorResponse 형식으로 내려준다.

## 인증 실패 코드

- `UNAUTHORIZED`: `Authorization: Bearer` 토큰이 없을 때 내려준다.
- `EXPIRED_TOKEN`: 서명은 올바르지만 유효 시간이 지난 토큰일 때 내려준다.
- `INVALID_TOKEN`: 서명이 맞지 않거나 형식이 잘못된 토큰일 때 내려준다.
- FE는 `EXPIRED_TOKEN`일 때만 토큰 재발급을 시도하고, 재발급이 실패하거나 다른 401 코드면 토큰을 지우고 로그인으로 보낸다.
- `FORBIDDEN`(403)은 인증은 됐지만 권한이 없다는 뜻이므로 재발급이나 재로그인을 시도하지 않는다.

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

### 토큰 만료 응답

```json
{
  "success": false,
  "code": "EXPIRED_TOKEN",
  "message": "로그인이 만료되었습니다. 다시 로그인해 주세요.",
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
