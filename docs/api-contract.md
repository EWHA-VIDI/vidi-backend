# API Contract

## Error Response

모든 애플리케이션 오류와 인증·인가 오류는 같은 형식을 사용한다.

```json
{
  "code": "COMMON_INVALID_INPUT",
  "message": "요청 값을 확인해 주세요.",
  "traceId": "82f23fb4-5bd6-4dc3-b3eb-9e02b5472a38"
}
```

- `code`: 프론트 분기와 운영 조회에 사용하는 안정적인 문자열 코드
- `message`: 사용자 또는 조사관에게 제공할 메시지
- `traceId`: 응답 헤더 `X-Trace-Id`와 같은 요청 추적값

`ErrorCode`, `ErrorResponse`, `BusinessException`, 전역 예외 처리기와 Security 오류 핸들러는 이 계약을 따른다.

## Pagination Response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "hasNext": false
}
```

- `page`는 0부터 시작한다.
- 정렬 정보는 요청 파라미터로 전달하고 응답에는 중복해서 넣지 않는다.
- 페이지 응답은 `PageResponse<T>`로 변환해 반환한다.
