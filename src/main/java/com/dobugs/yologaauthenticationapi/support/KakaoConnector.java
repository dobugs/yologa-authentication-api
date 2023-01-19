package com.dobugs.yologaauthenticationapi.support;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Component
public class KakaoConnector implements OAuthConnector {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private final OAuthProvider kakaoProvider;

    @Override
    public String generateOAuthUrl(final String redirectUrl) {
        return kakaoProvider.generateOAuthUrl(redirectUrl);
    }

    @Override
    public String requestAccessToken(final String authorizationCode, final String redirectUrl) {
        return null;
    }
}
