package com.dobugs.yologaauthenticationapi.support;

import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserResponse;

public class FakeOAuthConnector implements OAuthConnector {

    private final OAuthProvider provider = new FakeOAuthProvider();

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        return provider.generateOAuthUrl(redirectUrl, referrer);
    }

    @Override
    public TokenResponse requestToken(final String authorizationCode, final String redirectUrl) {
        return new TokenResponse(
            "accessToken", 1_000,
            "refreshToken", 10_000,
            "Bearer"
        );
    }

    @Override
    public UserResponse requestUserInfo(final String tokenType, final String accessToken) {
        return new UserResponse(String.valueOf(123456789));
    }

    @Override
    public TokenResponse requestAccessToken(final String refreshToken) {
        return new TokenResponse(
            "accessToken", 1_000,
            "refreshToken", 10_000,
            "Bearer"
        );
    }
}
