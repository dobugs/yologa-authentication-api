package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface OAuthProvider {

    Map<String, String> params = new HashMap<>();

    String generateOAuthUrl(String redirectUrl);

    default String concatParams() {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }
}
