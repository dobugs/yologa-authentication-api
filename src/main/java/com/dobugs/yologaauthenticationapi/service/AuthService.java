package com.dobugs.yologaauthenticationapi.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.OAuthToken;
import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthProviderRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final OAuthConnector googleConnector;
    private final OAuthConnector kakaoConnector;
    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;
    private final TokenGenerator tokenGenerator;

    public OAuthLinkResponse generateOAuthUrl(final OAuthRequest request) {
        final OAuthConnector oAuthConnector = selectConnector(request.provider());
        final String redirectUrl = decode(request.redirect_url());
        final String referrer = decode(request.referrer());

        final String oAuthUrl = oAuthConnector.generateOAuthUrl(redirectUrl, referrer);
        return new OAuthLinkResponse(oAuthUrl);
    }

    @Transactional
    public OAuthTokenResponse login(final OAuthRequest request, final OAuthCodeRequest codeRequest) {
        final String provider = request.provider();
        final OAuthConnector oAuthConnector = selectConnector(provider);
        final String redirectUrl = decode(request.redirect_url());
        final String authorizationCode = decode(codeRequest.authorizationCode());

        final TokenResponse tokenResponse = oAuthConnector.requestToken(authorizationCode, redirectUrl);
        final UserResponse userResponse = oAuthConnector.requestUserInfo(tokenResponse.tokenType(), tokenResponse.accessToken());

        final Long memberId = saveMember(provider, tokenResponse, userResponse);
        final ServiceTokenResponse serviceTokenResponse = tokenGenerator.create(memberId, tokenResponse);

        return new OAuthTokenResponse(serviceTokenResponse.accessToken(), serviceTokenResponse.refreshToken());
    }

    @Transactional
    public OAuthTokenResponse reissue(final OAuthProviderRequest request, final String serviceToken) {
        final OAuthConnector oAuthConnector = selectConnector(request.provider());
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final String refreshToken = decode(userTokenResponse.token());

        validateTheExistenceOfRefreshToken(userTokenResponse.memberId(), refreshToken);
        final TokenResponse response = oAuthConnector.requestAccessToken(refreshToken);
        restoreRefreshToken(userTokenResponse.memberId(), response.refreshToken());
        return new OAuthTokenResponse(response.accessToken(), response.refreshToken());
    }

    private Long saveMember(final String provider, final TokenResponse tokenResponse, final UserResponse userResponse) {
        final Member savedMember = memberRepository.findByOauthId(userResponse.oAuthId())
            .orElseGet(() -> memberRepository.save(new Member(userResponse.oAuthId())));

        final OAuthToken oAuthToken = OAuthToken.login(
            savedMember.getId(), Provider.findOf(provider),
            tokenResponse.refreshToken(), (long) tokenResponse.refreshTokenExpiresIn()
        );
        tokenRepository.save(oAuthToken);

        return savedMember.getId();
    }

    private void validateTheExistenceOfRefreshToken(final Long memberId, final String refreshToken) {
        if (!tokenRepository.existRefreshToken(memberId, refreshToken)) {
            throw new IllegalArgumentException("잘못된 refresh token 입니다.");
        }
    }

    private void restoreRefreshToken(final Long memberId, final String refreshToken) {
        tokenRepository.saveRefreshToken(memberId, refreshToken);
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

    private String decode(final String encoded) {
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
