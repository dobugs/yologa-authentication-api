package com.dobugs.yologaauthenticationapi.support;

public interface OAuthConnector {

    String generateOAuthUrl(String redirectUrl);

    String requestAccessToken(String authorizationCode, String redirectUrl);
}
