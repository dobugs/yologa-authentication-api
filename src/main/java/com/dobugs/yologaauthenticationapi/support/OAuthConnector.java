package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.client.RestTemplate;

public interface OAuthConnector {

    RestTemplate REST_TEMPLATE = new RestTemplate();

    String generateOAuthUrl(String redirectUrl);

    String requestAccessToken(String authorizationCode, String redirectUrl);
}
