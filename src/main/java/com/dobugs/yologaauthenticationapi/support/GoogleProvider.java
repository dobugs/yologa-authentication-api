package com.dobugs.yologaauthenticationapi.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class GoogleProvider implements OAuthProvider {

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

    @Override
    public String generateOAuthUrl(final String redirectUrl) {
        params.put("redirect_uri", redirectUrl);
        return authUrl + "?" + concatParams();
    }

    private void setParams() {
        params.put("scope", scope);
        params.put("response_type", "code");
        params.put("client_id", clientId);
    }
}