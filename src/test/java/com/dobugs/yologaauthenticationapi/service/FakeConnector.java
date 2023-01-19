package com.dobugs.yologaauthenticationapi.service;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

public class FakeConnector implements OAuthConnector {

    private final OAuthProvider provider = new FakeProvider();

    @Override
    public String generateOAuthUrl(final String redirectUrl) {
        return provider.generateOAuthUrl(redirectUrl);
    }

    @Override
    public String requestAccessToken(final String authorizationCode, final String redirectUrl) {
        return null;
    }
}
