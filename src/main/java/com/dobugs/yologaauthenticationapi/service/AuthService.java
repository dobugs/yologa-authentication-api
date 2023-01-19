package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthConnector;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final OAuthConnector googleConnector;
    private final OAuthConnector kakaoConnector;

    public OAuthLinkResponse generateOAuthUrl(final OAuthRequest request) {
        final OAuthConnector oAuthConnector = selectConnector(request.provider());
        final String oAuthUrl = oAuthConnector.generateOAuthUrl(request.redirect_url());

        return new OAuthLinkResponse(oAuthUrl);
    }

    @Transactional
    public OAuthTokenResponse login(final OAuthRequest request, final OAuthCodeRequest codeRequest) {
        final OAuthConnector oAuthConnector = selectConnector(request.provider());
        final String accessToken = oAuthConnector.requestAccessToken(
            codeRequest.authorizationCode(),
            request.redirect_url()
        );
        return null;
    }

    private OAuthConnector selectConnector(final String provider) {
        if (Provider.GOOGLE.getName().equals(provider)) {
            return googleConnector;
        }
        if (Provider.KAKAO.getName().equals(provider)) {
            return kakaoConnector;
        }
        throw new IllegalArgumentException(String.format("잘못된 provider 입니다. [%s]", provider));
    }
}
