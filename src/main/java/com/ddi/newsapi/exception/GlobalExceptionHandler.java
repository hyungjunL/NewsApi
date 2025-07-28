package com.ddi.newsapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 모든 @RestController에서 발생하는 예외를 처리하는 전역 핸들러로 지정합니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * UserService에서 발생하는 IllegalArgumentException을 처리합니다.
     * 이 예외는 주로 사용자 입력값이 비즈니스 규칙(예: 아이디 중복)에 맞지 않을 때 발생합니다.
     *
     * @param ex 발생한 예외 객체
     * @return 409 Conflict 상태 코드와 에러 메시지를 담은 ResponseEntity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        // 로그에는 어떤 예외가 발생했는지 전체 스택 트레이스를 기록하는 것이 좋습니다.
        log.error("IllegalArgumentException occurred: {}", ex.getMessage(), ex);

        // 클라이언트에게는 "이미 존재하는 리소스와 충돌했다"는 의미의 409 Conflict가 더 적합합니다.
        // 400 Bad Request도 좋은 선택입니다.
        HttpStatus status = HttpStatus.CONFLICT; // 409 Conflict

        // ErrorResponse DTO를 사용하여 일관된 형식의 응답 본문을 생성합니다.
        ErrorResponse errorResponse = new ErrorResponse(status.value(), ex.getMessage());

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 예상치 못한 모든 서버 내부 예외를 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return 500 Internal Server Error 상태 코드와 일반적인 에러 메시지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        ErrorResponse errorResponse = new ErrorResponse(status.value(), "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");

        return new ResponseEntity<>(errorResponse, status);
    }
}