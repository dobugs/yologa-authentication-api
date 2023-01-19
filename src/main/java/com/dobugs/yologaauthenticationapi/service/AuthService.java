package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.GoogleConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final OAuthProvider googleProvider;
    private final OAuthProvider kakaoProvider;
    private final GoogleConnector googleConnector;

    public OAuthLinkResponse generateOAuthUrl(final OAuthRequest request) {
        final OAuthProvider oAuthProvider = selectProvider(request.provider());
        final String oAuthUrl = oAuthProvider.generateOAuthUrl(request.redirect_url());

        return new OAuthLinkResponse(oAuthUrl);
    }

    @Transactional
    public OAuthTokenResponse login(final OAuthRequest request, final OAuthCodeRequest codeRequest) {
        final String accessToken = googleConnector.requestAccessToken(
            codeRequest.authorizationCode(),
            request.redirect_url()
        );

        return null;
    }

    private OAuthProvider selectProvider(final String provider) {
        if (Provider.GOOGLE.getName().equals(provider)) {
            return googleProvider;
        }
        if (Provider.KAKAO.getName().equals(provider)) {
            return kakaoProvider;
        }
        throw new IllegalArgumentException(String.format("잘못된 provider 입니다. [%s]", provider));
    }
}
