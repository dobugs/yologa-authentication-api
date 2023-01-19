package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import lombok.Getter;

@Getter
@Component
public class KakaoProvider implements OAuthProvider {

    private static final Map<String, String> params = new HashMap<>();

    private final String clientId;
    private final String authUrl;
    private final String accessTokenUrl;
    private final String grantType;

    public KakaoProvider(
        @Value("${oauth2.kakao.client.id}") final String clientId,
        @Value("${oauth2.kakao.url.auth}") final String authUrl,
        @Value("${oauth2.google.url.token}") final String accessTokenUrl,
        @Value("${oauth2.google.grant-type}") final String grantType
    ) {
        this.clientId = clientId;
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
        return null;
    }

    private void setParams() {
        params.put("client_id", clientId);
        params.put("response_type", "code");
    }
}
