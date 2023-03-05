package com.dobugs.yologaauthenticationapi.support;

import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthUserResponse;

public class FakeOAuthConnector implements OAuthConnector {

    private final OAuthProvider provider = new FakeOAuthProvider();

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        return provider.generateOAuthUrl(redirectUrl, referrer);
    }

    @Override
    public OAuthTokenResponse requestToken(final String authorizationCode, final String redirectUrl) {
        return new OAuthTokenResponse(
            "accessToken", 1_000,
            "refreshToken", 10_000,
            "Bearer"
        );
    }

    @Override
    public OAuthUserResponse requestUserInfo(final String tokenType, final String accessToken) {
        return new OAuthUserResponse(String.valueOf(123456789));
    }

    @Override
    public OAuthTokenResponse requestAccessToken(final String refreshToken) {
        return new OAuthTokenResponse(
            "accessToken", 1_000,
            "refreshToken", 10_000,
            "Bearer"
        );
    }
}
