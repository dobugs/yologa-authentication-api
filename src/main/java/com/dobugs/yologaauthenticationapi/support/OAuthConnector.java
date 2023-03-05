package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthUserResponse;

public interface OAuthConnector {

    RestTemplate REST_TEMPLATE = new RestTemplate();

    String generateOAuthUrl(String redirectUrl, String referrer);

    OAuthTokenResponse requestToken(String authorizationCode, String redirectUrl);

    OAuthUserResponse requestUserInfo(String tokenType, String accessToken);

    OAuthTokenResponse requestAccessToken(String refreshToken);
}
