package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;

import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthLinkRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final OAuthProvider googleProvider;
    private final OAuthProvider kakaoProvider;

    public OAuthLinkResponse generateOAuthUrl(final OAuthLinkRequest request) {
        final OAuthProvider oAuthProvider = selectProvider(request.provider());
        final String oAuthUrl = oAuthProvider.generateOAuthUrl(request.redirect_url());

        return new OAuthLinkResponse(oAuthUrl);
    }

    private OAuthProvider selectProvider(final String provider) {
        if (Provider.GOOGLE.name().equals(provider)) {
            return googleProvider;
        }
        return kakaoProvider;
    }
}
