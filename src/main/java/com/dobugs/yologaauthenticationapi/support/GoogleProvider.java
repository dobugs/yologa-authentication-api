package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class GoogleProvider {

    private static final Map<String, String> params = new HashMap<>();

    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String authUrl;

    public GoogleProvider(
        @Value("${oauth2.google.client.id}") final String clientId,
        @Value("${oauth2.google.client.secret}") final String clientSecret,
        @Value("${oauth2.google.scope}") final String scope,
        @Value("${oauth2.google.url.auth}") final String authUrl
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.authUrl = authUrl;
        setParams();
    }

    public String generateOAuthUrl(final String redirectUrl) {
        params.put("redirect_uri", redirectUrl);
        return authUrl + "?" + concatParams();
    }

    private String concatParams() {
        return params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    private void setParams() {
        params.put("scope", scope);
        params.put("response_type", "code");
        params.put("client_id", clientId);
    }
}
