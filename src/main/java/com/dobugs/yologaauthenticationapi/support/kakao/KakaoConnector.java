package com.dobugs.yologaauthenticationapi.support.kakao;

import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.KakaoTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserResponse;

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
    public TokenResponse requestToken(final String authorizationCode, final String redirectUrl) {
        final KakaoTokenResponse response = connect(authorizationCode, redirectUrl);
        return new TokenResponse(response.access_token(), response.refresh_token(), response.token_type());
    }

    @Override
    public UserResponse requestUserInfo(final String tokenType, final String accessToken) {
        return null;
    }

    private KakaoTokenResponse connect(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<KakaoTokenResponse> response = REST_TEMPLATE.postForEntity(
            kakaoProvider.generateTokenUrl(authorizationCode, redirectUrl),
            kakaoProvider.createTokenEntity(),
            KakaoTokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("kakao 와의 연결에 실패하였습니다."));
    }

    private void validateConnectionResponseIsSuccess(final ResponseEntity<KakaoTokenResponse> response) {
        final HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("kakao 와의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
