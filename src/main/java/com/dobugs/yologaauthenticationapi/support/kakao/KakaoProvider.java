package com.dobugs.yologaauthenticationapi.support.kakao;

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
public class KakaoProvider implements OAuthProvider {

    private final String clientId;
    private final String authUrl;
    private final String accessTokenUrl;
    private final String userInfoUrl;
    private final String logoutUrl;
    private final String grantType;

    public KakaoProvider(
        @Value("${oauth2.kakao.client.id}") final String clientId,
        @Value("${oauth2.kakao.url.auth}") final String authUrl,
        @Value("${oauth2.kakao.url.token}") final String accessTokenUrl,
        @Value("${oauth2.kakao.url.userinfo}") final String userInfoUrl,
        @Value("${oauth2.kakao.url.logout}") final String logoutUrl,
        @Value("${oauth2.kakao.grant-type}") final String grantType
    ) {
        this.clientId = clientId;
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
        params.put("referrer", referrer);
        return authUrl + "?" + concatParams(params);
    }

    @Override
    public String generateTokenUrl(final String authorizationCode, final String redirectUrl) {
        final Map<String, String> params = new HashMap<>();
        params.put("code", authorizationCode);
        params.put("client_id", clientId);
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
        params.put("grant_type", "refresh_token");
        params.put("client_id", clientId);
        params.put("refresh_token", refreshToken);
        return accessTokenUrl + "?" + concatParams(params);
    }

    @Override
    public String generateLogoutUrl(final String token) {
        return logoutUrl;
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
    public HttpEntity<MultiValueMap<String, String>> createLogoutEntity(final String tokenType, final String accessToken) {
        return new HttpEntity<>(createAuthorizationHeaders(tokenType, accessToken));
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
