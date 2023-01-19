package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    @Override
    public HttpEntity<MultiValueMap<String, String>> createEntity(
        final String authorizationCode,
        final String redirectUrl
    ) {
        return new HttpEntity<>(
            createBody(authorizationCode, redirectUrl),
            createHeaders()
        );
    }

    private MultiValueMap<String, String> createBody(final String authorizationCode, final String redirectUrl) {
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUrl);
        body.add("grant_type", grantType);
        return body;
    }

    private HttpHeaders createHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private void setParams() {
        params.put("scope", scope);
        params.put("response_type", "code");
        params.put("client_id", clientId);
    }
}
