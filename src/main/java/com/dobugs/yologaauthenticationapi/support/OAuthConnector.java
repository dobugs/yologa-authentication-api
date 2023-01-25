package com.dobugs.yologaauthenticationapi.support;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;

public interface OAuthConnector {

    RestTemplate REST_TEMPLATE = new RestTemplate();

    String generateOAuthUrl(String redirectUrl, String referrer);

    String requestAccessToken(String authorizationCode, String redirectUrl);

    default void validateConnectionResponseIsSuccess(final ResponseEntity<TokenResponse> response) {
        final HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("외부 서버와의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
