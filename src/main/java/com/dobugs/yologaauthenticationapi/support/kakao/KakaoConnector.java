package com.dobugs.yologaauthenticationapi.support.kakao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

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
