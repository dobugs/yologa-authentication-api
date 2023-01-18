package com.dobugs.yologaauthenticationapi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

public class FakeProvider implements OAuthProvider {

    private final Map<String, String> params = new HashMap<>();

    private static final String CLIENT_ID = "clientId";
    private static final String SCOPE = "scope";
    private static final String AUTH_URL = "authUrl";

    public FakeProvider() {
        setParams();
    }

    @Override
    public String generateOAuthUrl(final String redirectUrl) {
        params.put("redirect_uri", redirectUrl);
        return AUTH_URL + "?" + concatParams();
    }

    public String concatParams() {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    private void setParams() {
        params.put("scope", SCOPE);
        params.put("response_type", "code");
        params.put("client_id", CLIENT_ID);
    }
}
