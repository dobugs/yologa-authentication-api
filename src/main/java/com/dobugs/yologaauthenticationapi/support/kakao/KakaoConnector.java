package com.dobugs.yologaauthenticationapi.support.kakao;

import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.AccessTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.KakaoTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.KakaoUserResponse;
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
        final KakaoTokenResponse response = connectForToken(authorizationCode, redirectUrl);
        return new TokenResponse(response.access_token(), response.refresh_token(), response.token_type());
    }

    @Override
    public UserResponse requestUserInfo(final String tokenType, final String accessToken) {
        final KakaoUserResponse response = connectForUserInfo(tokenType, accessToken);
        return new UserResponse(response.id());
    }

    @Override
    public AccessTokenResponse requestAccessToken(final String refreshToken) {
        return null;
    }

    private KakaoTokenResponse connectForToken(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<KakaoTokenResponse> response = REST_TEMPLATE.postForEntity(
            kakaoProvider.generateTokenUrl(authorizationCode, redirectUrl),
            kakaoProvider.createTokenEntity(),
            KakaoTokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("kakao 와의 연결에 실패하였습니다."));
    }

    private KakaoUserResponse connectForUserInfo(final String tokenType, final String accessToken) {
        final ResponseEntity<KakaoUserResponse> response = REST_TEMPLATE.exchange(
            kakaoProvider.generateUserInfoUrl(),
            HttpMethod.GET,
            kakaoProvider.createUserEntity(tokenType, accessToken),
            KakaoUserResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("Google 의 사용자 정보를 가져오는 과정에서 연결에 실패하였습니다."));
    }

    private void validateConnectionResponseIsSuccess(final ResponseEntity<?> response) {
        final HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("kakao 와의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
