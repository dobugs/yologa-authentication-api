package com.dobugs.yologaauthenticationapi.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.OAuthToken;
import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.ServiceTokenResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.request.OAuthLogoutRequest;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthUserResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthService {

    private final OAuthConnector googleConnector;
    private final OAuthConnector kakaoConnector;
    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;
    private final TokenGenerator tokenGenerator;

    @Transactional(readOnly = true)
    public OAuthLinkResponse generateOAuthUrl(final OAuthRequest request) {
        final OAuthConnector oAuthConnector = selectConnector(request.provider());
        final String oAuthUrl = oAuthConnector.generateOAuthUrl(
            decode(request.redirect_url()),
            decode(request.referrer())
        );
        return new OAuthLinkResponse(oAuthUrl);
    }

    public ServiceTokenResponse login(final OAuthRequest request, final OAuthCodeRequest codeRequest) {
        final OAuthConnector oAuthConnector = selectConnector(request.provider());
        final OAuthTokenResponse oAuthTokenResponse = oAuthConnector.requestToken(
            decode(codeRequest.authorizationCode()),
            decode(request.redirect_url())
        );
        final OAuthUserResponse oAuthUserResponse = oAuthConnector.requestUserInfo(oAuthTokenResponse.tokenType(), oAuthTokenResponse.accessToken());

        final OAuthTokenDto oAuthTokenDto = tokenGenerator.setUpExpiration(oAuthTokenResponse);
        final Long memberId = saveMember(request.provider(), oAuthTokenDto, oAuthUserResponse);
        final ServiceTokenDto serviceTokenDto = tokenGenerator.create(memberId, request.provider(), oAuthTokenDto);

        return new ServiceTokenResponse(serviceTokenDto.accessToken(), serviceTokenDto.refreshToken());
    }

    public ServiceTokenResponse reissue(final ServiceToken serviceToken) {
        final OAuthConnector oAuthConnector = selectConnector(serviceToken.provider());

        final OAuthTokenResponse response = oAuthConnector.requestAccessToken(serviceToken.token());
        final OAuthTokenDto oAuthTokenDto = tokenGenerator.setUpExpiration(response);
        restoreRefreshToken(serviceToken.memberId(), oAuthTokenDto.refreshToken());
        final ServiceTokenDto serviceTokenDto = tokenGenerator.create(serviceToken.memberId(), serviceToken.provider(), oAuthTokenDto);
        return new ServiceTokenResponse(serviceTokenDto.accessToken(), serviceTokenDto.refreshToken());
    }

    public void logout(final ServiceToken serviceToken) {
        final OAuthConnector oAuthConnector = selectConnector(serviceToken.provider());

        final String refreshToken = findRefreshToken(serviceToken.memberId());
        tokenRepository.delete(serviceToken.memberId());
        oAuthConnector.logout(new OAuthLogoutRequest(serviceToken.token(), refreshToken, serviceToken.tokenType()));
    }

    private Long saveMember(final String provider, final OAuthTokenDto oAuthTokenDto, final OAuthUserResponse OAuthUserResponse) {
        final Member savedMember = saveMemberToMySQL(OAuthUserResponse);
        saveMemberToRedis(provider, oAuthTokenDto, savedMember);
        return savedMember.getId();
    }

    private Member saveMemberToMySQL(final OAuthUserResponse OAuthUserResponse) {
        final Member savedMember = memberRepository.findByOauthId(OAuthUserResponse.oAuthId())
            .orElseGet(() -> {
                final Member member = new Member(OAuthUserResponse.oAuthId());
                memberRepository.save(member);
                member.init();
                return member;
            });
        if (!savedMember.isArchived()) {
            savedMember.rejoin();
        }
        return savedMember;
    }

    private void saveMemberToRedis(final String provider, final OAuthTokenDto oAuthTokenDto, final Member savedMember) {
        final OAuthToken oAuthToken = OAuthToken.login(
            savedMember.getId(), Provider.findOf(provider),
            oAuthTokenDto.refreshToken(), oAuthTokenDto.refreshTokenExpiresIn()
        );
        tokenRepository.save(oAuthToken);
    }

    private void restoreRefreshToken(final Long memberId, final String refreshToken) {
        tokenRepository.saveRefreshToken(memberId, refreshToken);
    }

    private String findRefreshToken(final Long memberId) {
        return tokenRepository.findRefreshToken(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("로그인이 필요합니다. [%d]", memberId)));
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
