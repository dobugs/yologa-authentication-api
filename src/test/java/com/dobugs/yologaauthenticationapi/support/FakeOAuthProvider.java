package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public class FakeOAuthProvider implements OAuthProvider {

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String SCOPE = "scope";
    private static final String AUTH_URL = "authUrl";
    private static final String TOKEN_URL = "tokenUrl";
    private static final String USER_INFO_URL = "userInfoUrl";

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
    public String generateUserInfoUrl() {
        return USER_INFO_URL;
    }

    @Override
    public String generateAccessTokenUrl(final String refreshToken) {
        final Map<String, String> params = new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("client_secret", CLIENT_SECRET);
        params.put("refresh_token", refreshToken);
        params.put("grant_type", "refresh_token");
        return TOKEN_URL + "?" + concatParams(params);
    }

    @Override
    public String generateLogoutUrl(final String token) {
        return null;
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createTokenEntity() {
        return new HttpEntity<>(createTokenHeaders());
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createUserEntity(final String tokenType, final String accessToken) {
        return new HttpEntity<>(createUserHeaders(tokenType, accessToken));
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createAccessTokenEntity() {
        return new HttpEntity<>(createTokenHeaders());
    }

    @Override
    public HttpEntity<MultiValueMap<String, String>> createLogoutEntity() {
        return new HttpEntity<>(createTokenHeaders());
    }

    private HttpHeaders createTokenHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private HttpHeaders createUserHeaders(final String tokenType, final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.join(" ", tokenType, accessToken));
        return headers;
    }
}
