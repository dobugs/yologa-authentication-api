package com.dobugs.yologaauthenticationapi.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class KakaoProvider implements OAuthProvider {

    private static final Map<String, String> params = new HashMap<>();

    private final String clientId;
    private final String authUrl;

    public KakaoProvider(
        @Value("${oauth2.kakao.client.id}") final String clientId,
        @Value("${oauth2.kakao.url.auth}") final String authUrl
    ) {
        this.clientId = clientId;
        this.authUrl = authUrl;
        setParams();
    }

    @Override
    public String generateOAuthUrl(final String redirectUrl) {
        params.put("redirect_uri", redirectUrl);
        return authUrl + "?" + concatParams(params);
    }

    private void setParams() {
        params.put("client_id", clientId);
        params.put("response_type", "code");
    }
}
