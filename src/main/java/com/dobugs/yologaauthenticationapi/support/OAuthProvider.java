package com.dobugs.yologaauthenticationapi.support;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

public interface OAuthProvider {

    String generateOAuthUrl(String redirectUrl);

    HttpEntity<MultiValueMap<String, String>> createEntity(String authorizationCode, String redirectUrl);

    String getAccessTokenUrl();

    default String concatParams(final Map<String, String> params) {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }
}
