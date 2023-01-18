package com.dobugs.yologaauthenticationapi.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class KakaoProvider implements OAuthProvider {

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
        return authUrl + "?" + concatParams();
    }

    private void setParams() {
        params.put("client_id", clientId);
        params.put("response_type", "code");
    }
}
