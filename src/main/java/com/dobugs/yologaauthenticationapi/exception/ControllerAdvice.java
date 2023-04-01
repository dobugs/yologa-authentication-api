package com.dobugs.yologaauthenticationapi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dobugs.yologaauthenticationapi.auth.exception.AuthorizationException;
import com.dobugs.yologaauthenticationapi.exception.dto.response.ExceptionResponse;
import com.dobugs.yologaauthenticationapi.support.exception.OAuthException;
import com.dobugs.yologaauthenticationapi.support.logging.UnhandledExceptional;

import io.awspring.cloud.s3.S3Exception;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

@RestControllerAdvice
public class ControllerAdvice {

    private static final int UNAUTHORIZED = 401;

    @UnhandledExceptional
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleInternalServerError(Exception e) {
        final ExceptionResponse response = ExceptionResponse.from(e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(Exception e) {
        final ExceptionResponse response = ExceptionResponse.from(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ExceptionResponse> handleMalformedJWT(MalformedJwtException e) {
        final ExceptionResponse response = ExceptionResponse.from("잘못된 형식의 JWT 입니다.");
        return ResponseEntity.status(UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ExceptionResponse> handleUnsupportedJWT(UnsupportedJwtException e) {
        final String message = "지원하지 않는 JWT 입니다.";
        final ExceptionResponse response = ExceptionResponse.from(message, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ExceptionResponse> handleSignatureException(SignatureException e) {
        final ExceptionResponse response = ExceptionResponse.from("두벅스의 JWT 가 아닙니다.");
        return ResponseEntity.status(UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionResponse> handleExpiredJwtException(ExpiredJwtException e) {
        final String message = "토큰의 만료 시간이 지났습니다.";
        final ExceptionResponse response = ExceptionResponse.from(message, e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthorizationException(AuthorizationException e) {
        final ExceptionResponse response = ExceptionResponse.from(e.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<ExceptionResponse> handleOAuthException(OAuthException e) {
        final ExceptionResponse response = ExceptionResponse.from(e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler({AwsServiceException.class, S3Exception.class})
    public ResponseEntity<ExceptionResponse> handleSdkServiceException(AwsServiceException e) {
        final String message = "S3 에 연결하기 위한 과정에서 문제가 발생하였습니다.";
        final ExceptionResponse response = ExceptionResponse.from(message, e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<ExceptionResponse> handleSdkClientException(SdkClientException e) {
        final String message = "S3 와의 연결에서 문제가 발생하였습니다.";
        final ExceptionResponse response = ExceptionResponse.from(message, e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
