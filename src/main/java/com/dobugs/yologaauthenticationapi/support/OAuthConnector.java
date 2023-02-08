package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.dto.response.AccessTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserResponse;

public interface OAuthConnector {

    RestTemplate REST_TEMPLATE = new RestTemplate();

    String generateOAuthUrl(String redirectUrl, String referrer);

    TokenResponse requestToken(String authorizationCode, String redirectUrl);

    UserResponse requestUserInfo(String tokenType, String accessToken);

    AccessTokenResponse requestAccessToken(String refreshToken);
}
