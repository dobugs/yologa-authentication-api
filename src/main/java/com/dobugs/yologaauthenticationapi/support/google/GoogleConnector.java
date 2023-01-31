package com.dobugs.yologaauthenticationapi.support.google;

import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.GoogleTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.GoogleUserResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserResponse;

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
    public TokenResponse requestToken(final String authorizationCode, final String redirectUrl) {
        final GoogleTokenResponse response = connectForToken(authorizationCode, redirectUrl);
        return new TokenResponse(response.access_token(), response.refresh_token(), response.token_type());
    }

    @Override
    public UserResponse requestUserInfo(final String tokenType, final String accessToken) {
        final GoogleUserResponse response = connectForUserInfo(tokenType, accessToken);
        return new UserResponse(response.id());
    }

    private GoogleTokenResponse connectForToken(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<GoogleTokenResponse> response = REST_TEMPLATE.postForEntity(
            googleProvider.generateTokenUrl(authorizationCode, redirectUrl),
            googleProvider.createTokenEntity(),
            GoogleTokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("Google 의 Token 정보를 가져오는 과정에서 연결에 실패하였습니다."));
    }

    private GoogleUserResponse connectForUserInfo(final String tokenType, final String accessToken) {
        final ResponseEntity<GoogleUserResponse> response = REST_TEMPLATE.exchange(
            googleProvider.generateUserInfoUrl(),
            HttpMethod.GET,
            googleProvider.createUserEntity(tokenType, accessToken),
            GoogleUserResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("Google 의 사용자 정보를 가져오는 과정에서 연결에 실패하였습니다."));
    }

    private void validateConnectionResponseIsSuccess(final ResponseEntity<?> response) {
        final HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("Google 과의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
