package com.dobugs.yologaauthenticationapi.service;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;

public class FakeConnector implements OAuthConnector {

    private final OAuthProvider provider = new FakeProvider();

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        return provider.generateOAuthUrl(redirectUrl, referrer);
    }

    @Override
    public TokenResponse requestAccessToken(final String authorizationCode, final String redirectUrl) {
        return null;
    }
}
