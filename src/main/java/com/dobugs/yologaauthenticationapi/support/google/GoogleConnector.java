package com.dobugs.yologaauthenticationapi.support.google;

import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.GoogleTokenResponse;
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
    public TokenResponse requestAccessToken(final String authorizationCode, final String redirectUrl) {
        final GoogleTokenResponse response = connect(authorizationCode, redirectUrl);
        return new TokenResponse(response.access_token(), response.refresh_token());
    }

    private GoogleTokenResponse connect(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<GoogleTokenResponse> response = REST_TEMPLATE.postForEntity(
            googleProvider.generateTokenUrl(authorizationCode, redirectUrl),
            googleProvider.createEntity(),
            GoogleTokenResponse.class
        );
        validateConnectionResponseIsSuccess(response);
        return Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new IllegalArgumentException("Google 과의 연결에 실패하였습니다."));
    }

    private void validateConnectionResponseIsSuccess(final ResponseEntity<GoogleTokenResponse> response) {
        final HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("Google 과의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
