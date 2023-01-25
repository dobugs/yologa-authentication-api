package com.dobugs.yologaauthenticationapi.support.kakao;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Component
public class KakaoConnector implements OAuthConnector {

    private final OAuthProvider kakaoProvider;

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        return kakaoProvider.generateOAuthUrl(redirectUrl, referrer);
    }

    @Override
    public String requestAccessToken(final String authorizationCode, final String redirectUrl) {
        final TokenResponse response = connect(authorizationCode, redirectUrl);
        return response.accessToken();
    }

    private TokenResponse connect(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<TokenResponse> response = REST_TEMPLATE.postForEntity(
            kakaoProvider.generateTokenUrl(authorizationCode, redirectUrl),
            kakaoProvider.createEntity(authorizationCode, redirectUrl),
            TokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("kakao 와의 연결에 실패하였습니다."));
    }
}
