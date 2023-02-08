package com.dobugs.yologaauthenticationapi.support;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

public interface OAuthProvider {

    String generateOAuthUrl(String redirectUrl, String referrer);

    String generateTokenUrl(String authorizationCode, String redirectUrl);

    String generateUserInfoUrl();

    String generateAccessTokenUrl(String refreshToken);

    HttpEntity<MultiValueMap<String, String>> createTokenEntity();

    HttpEntity<MultiValueMap<String, String>> createUserEntity(String tokenType, String accessToken);

    HttpEntity<MultiValueMap<String, String>> createAccessTokenEntity();

    default String concatParams(final Map<String, String> params) {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }
}
