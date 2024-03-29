package com.dobugs.yologaauthenticationapi.support.google;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

import lombok.Getter;

@Getter
@Component
public class GoogleProvider implements OAuthProvider {

    private final String clientId;
    private final String clientSecret;
    private final String accessType;
    private final String scope;
    private final String authUrl;
    private final String accessTokenUrl;
    private final String userInfoUrl;
    private final String logoutUrl;
    private final String grantType;

    public GoogleProvider(
        @Value("${oauth2.google.client.id}") final String clientId,
        @Value("${oauth2.google.client.secret}") final String clientSecret,
        @Value("${oauth2.google.scope}") final String scope,
        @Value("${oauth2.google.access-type}") final String accessType,
        @Value("${oauth2.google.url.auth}") final String authUrl,
        @Value("${oauth2.google.url.token}") final String accessTokenUrl,
        @Value("${oauth2.google.url.userinfo}") final String userInfoUrl,
        @Value("${oauth2.google.url.logout}") final String logoutUrl,
        @Value("${oauth2.google.grant-type}") final String grantType
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessType = accessType;
        this.scope = scope;
        this.authUrl = authUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.userInfoUrl = userInfoUrl;
        this.logoutUrl = logoutUrl;
        this.grantType = grantType;
    }

    @Override
    public String generateOAuthUrl(final String redirectUrl, final String referrer) {
        final Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("redirect_uri", redirectUrl);
        params.put("response_type", "code");
        params.put("scope", scope);
        params.put("access_type", accessType);
        params.put("referrer", referrer);
        return authUrl + "?" + concatParams(params);
    }

    @Override
    public String generateTokenUrl(final String authorizationCode, final String redirectUrl) {
        final Map<String, String> params = new HashMap<>();
        params.put("code", authorizationCode);
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUrl);
        params.put("grant_type", grantType);
        return accessTokenUrl + "?" + concatParams(params);
    }

    @Override
    public String generateUserInfoUrl() {
        return userInfoUrl;
    }

    @Override
    public String generateAccessTokenUrl(final String refreshToken) {
        final Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("refresh_token", refreshToken);
        params.put("grant_type", "refresh_token");
        return accessTokenUrl + "?" + concatParams(params);
    }

    @Override
    public String generateLogoutUrl(final String token) {
        final Map<String, String> params = new HashMap<>();
        params.put("token", token);
        return logoutUrl + "?" + concatParams(params);
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createTokenEntity() {
        return new HttpEntity<>(createBasicHeaders());
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createUserEntity(final String tokenType, final String accessToken) {
        return new HttpEntity<>(createAuthorizationHeaders(tokenType, accessToken));
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createAccessTokenEntity() {
        return new HttpEntity<>(createBasicHeaders());
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createLogoutEntity(final String tokenType, final String refreshToken) {
        return new HttpEntity<>(createBasicHeaders());
    }

    private HttpHeaders createBasicHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private HttpHeaders createAuthorizationHeaders(final String tokenType, final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", String.join(" ", tokenType, accessToken));
        return headers;
    }
}
