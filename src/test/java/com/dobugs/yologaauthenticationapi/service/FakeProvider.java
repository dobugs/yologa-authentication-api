package com.dobugs.yologaauthenticationapi.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

public class FakeProvider implements OAuthProvider {

    private static final String CLIENT_ID = "clientId";
    private static final String SCOPE = "scope";
    private static final String AUTH_URL = "authUrl";
    private static final String TOKEN_URL = "tokenUrl";

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        final Map<String, String> params = new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("redirect_uri", redirectUrl);
        params.put("response_type", "code");
        params.put("scope", SCOPE);
        params.put("referrer", referrer);
        return AUTH_URL + "?" + concatParams(params);
    }

    @Override
    public String generateTokenUrl(final String authorizationCode, final String redirectUrl) {
        final Map<String, String> params = new HashMap<>();
        params.put("code", authorizationCode);
        params.put("redirect_url", redirectUrl);
        return TOKEN_URL + "?" + concatParams(params);
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createEntity() {
        return null;
    }

    public String concatParams(final Map<String, String> params) {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }
}
