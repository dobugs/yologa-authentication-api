package com.dobugs.yologaauthenticationapi.support.google;

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
public class GoogleConnector implements OAuthConnector {

    private final OAuthProvider googleProvider;

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        return googleProvider.generateOAuthUrl(redirectUrl, referrer);
    }

    @Override
    public String requestAccessToken(final String authorizationCode, final String redirectUrl) {
        final TokenResponse response = connect(authorizationCode, redirectUrl);
        return response.accessToken();
    }

    private TokenResponse connect(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<TokenResponse> response = REST_TEMPLATE.postForEntity(
            googleProvider.getAccessTokenUrl(),
            googleProvider.createEntity(authorizationCode, redirectUrl),
            TokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("Google 과의 연결에 실패하였습니다."));
    }
}
