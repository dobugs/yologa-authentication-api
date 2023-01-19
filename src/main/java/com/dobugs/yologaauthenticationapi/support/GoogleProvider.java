package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class GoogleProvider implements OAuthProvider {

    private static final Map<String, String> params = new HashMap<>();

    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String authUrl;
    private final String accessTokenUrl;
    private final String grantType;

    public GoogleProvider(
        @Value("${oauth2.google.client.id}") final String clientId,
        @Value("${oauth2.google.client.secret}") final String clientSecret,
        @Value("${oauth2.google.scope}") final String scope,
        @Value("${oauth2.google.url.auth}") final String authUrl,
        @Value("${oauth2.google.url.token}") final String accessTokenUrl,
        @Value("${oauth2.google.grant-type}") final String grantType
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.authUrl = authUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.grantType = grantType;
        setParams();
    }

    @Override
    public String generateOAuthUrl(final String redirectUrl) {
        params.put("redirect_uri", redirectUrl);
        return authUrl + "?" + concatParams(params);
    }

    private void setParams() {
        params.put("scope", scope);
        params.put("response_type", "code");
        params.put("client_id", clientId);
    }
}
