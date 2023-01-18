package com.dobugs.yologaauthenticationapi.support;

import java.util.Map;
import java.util.stream.Collectors;

public interface OAuthProvider {

    String generateOAuthUrl(String redirectUrl);

    default String concatParams(final Map<String, String> params) {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }
}
