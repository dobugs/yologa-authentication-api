package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;

public interface OAuthConnector {

    RestTemplate REST_TEMPLATE = new RestTemplate();

    String generateOAuthUrl(String redirectUrl, String referrer);

    TokenResponse requestAccessToken(String authorizationCode, String redirectUrl);
}
