package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;

import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthLinkRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.support.GoogleProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final GoogleProvider googleProvider;

    public OAuthLinkResponse generateOAuthUrl(final OAuthLinkRequest request) {
        final String oAuthUrl = googleProvider.generateOAuthUrl(request.redirect_url());
        return new OAuthLinkResponse(oAuthUrl);
    }
}
