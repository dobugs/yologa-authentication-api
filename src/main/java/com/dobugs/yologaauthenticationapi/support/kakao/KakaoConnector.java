package com.dobugs.yologaauthenticationapi.support.kakao;

import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.KakaoTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.KakaoUserResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthUserResponse;
import com.dobugs.yologaauthenticationapi.support.exception.OAuthConnectionException;

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
    public OAuthTokenResponse requestToken(final String authorizationCode, final String redirectUrl) {
        final KakaoTokenResponse response = connectForToken(authorizationCode, redirectUrl);
        return new OAuthTokenResponse(response.access_token(), response.expires_in(), response.refresh_token(),
            response.refresh_token_expires_in(), response.token_type());
    }

    @Override
    public OAuthUserResponse requestUserInfo(final String tokenType, final String accessToken) {
        final KakaoUserResponse response = connectForUserInfo(tokenType, accessToken);
        return new OAuthUserResponse(response.id());
    }

    @Override
    public OAuthTokenResponse requestAccessToken(final String refreshToken) {
        final KakaoTokenResponse response = connectForAccessToken(refreshToken);
        final String returnedRefreshToken = selectRefreshToken(refreshToken, response);
        return new OAuthTokenResponse(response.access_token(), response.expires_in(), returnedRefreshToken,
            response.refresh_token_expires_in(), response.token_type());
    }

    @Override
    public void logout(final String token) {

    }

    private KakaoTokenResponse connectForToken(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<KakaoTokenResponse> response = REST_TEMPLATE.postForEntity(
            kakaoProvider.generateTokenUrl(authorizationCode, redirectUrl),
            kakaoProvider.createTokenEntity(),
            KakaoTokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new OAuthConnectionException("kakao 와의 연결에 실패하였습니다."));
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
            .orElseThrow(() -> new OAuthConnectionException("kakao 의 사용자 정보를 가져오는 과정에서 연결에 실패하였습니다."));
    }

    private KakaoTokenResponse connectForAccessToken(final String refreshToken) {
        final ResponseEntity<KakaoTokenResponse> response = REST_TEMPLATE.postForEntity(
            kakaoProvider.generateAccessTokenUrl(refreshToken),
            kakaoProvider.createAccessTokenEntity(),
            KakaoTokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new OAuthConnectionException("kakao 에서 Access Token 을 재발급 받는 과정에서 연결에 실패하였습니다."));
    }

    private String selectRefreshToken(final String refreshToken, final KakaoTokenResponse response) {
        if (response.refresh_token() != null) {
            return response.refresh_token();
        }
        return refreshToken;
    }

    private void validateConnectionResponseIsSuccess(final ResponseEntity<?> response) {
        final HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new OAuthConnectionException(String.format("kakao 와의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
