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
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.ServiceTokenResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthUserResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

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
        final String redirectUrl = decode(request.redirect_url());
        final String referrer = decode(request.referrer());

        final String oAuthUrl = oAuthConnector.generateOAuthUrl(redirectUrl, referrer);
        return new OAuthLinkResponse(oAuthUrl);
    }

    public ServiceTokenResponse login(final OAuthRequest request, final OAuthCodeRequest codeRequest) {
        final String provider = request.provider();
        final OAuthConnector oAuthConnector = selectConnector(provider);
        final String redirectUrl = decode(request.redirect_url());
        final String authorizationCode = decode(codeRequest.authorizationCode());

        final OAuthTokenResponse oAuthTokenResponse = oAuthConnector.requestToken(authorizationCode, redirectUrl);
        final OAuthUserResponse oAuthUserResponse = oAuthConnector.requestUserInfo(oAuthTokenResponse.tokenType(), oAuthTokenResponse.accessToken());

        final Long memberId = saveMember(provider, oAuthTokenResponse, oAuthUserResponse);
        final ServiceTokenDto serviceTokenDto = tokenGenerator.create(memberId, provider, oAuthTokenResponse);

        return new ServiceTokenResponse(serviceTokenDto.accessToken(), serviceTokenDto.refreshToken());
    }

    public ServiceTokenResponse reissue(final String serviceToken) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final OAuthConnector oAuthConnector = selectConnector(userTokenResponse.provider());
        final String refreshToken = decode(userTokenResponse.token());

        validateTheExistenceOfRefreshToken(userTokenResponse.memberId(), refreshToken);
        final OAuthTokenResponse response = oAuthConnector.requestAccessToken(refreshToken);
        restoreRefreshToken(userTokenResponse.memberId(), response.refreshToken());
        final ServiceTokenDto serviceTokenDto = tokenGenerator.create(userTokenResponse.memberId(), userTokenResponse.provider(), response);
        return new ServiceTokenResponse(serviceTokenDto.accessToken(), serviceTokenDto.refreshToken());
    }

    public void logout(final String serviceToken) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();

        validateLogged(memberId);
        tokenRepository.delete(memberId);
    }

    private Long saveMember(final String provider, final OAuthTokenResponse oAuthTokenResponse, final OAuthUserResponse OAuthUserResponse) {
        final Member savedMember = saveMemberToMySQL(OAuthUserResponse);
        saveMemberToRedis(provider, oAuthTokenResponse, savedMember);
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

    private void saveMemberToRedis(final String provider, final OAuthTokenResponse oAuthTokenResponse, final Member savedMember) {
        final OAuthToken oAuthToken = OAuthToken.login(
            savedMember.getId(), Provider.findOf(provider),
            oAuthTokenResponse.refreshToken(), (long) oAuthTokenResponse.refreshTokenExpiresIn()
        );
        tokenRepository.save(oAuthToken);
    }

    private void restoreRefreshToken(final Long memberId, final String refreshToken) {
        tokenRepository.saveRefreshToken(memberId, refreshToken);
    }

    private void validateLogged(final Long memberId) {
        if (!tokenRepository.exist(memberId)) {
            throw new IllegalArgumentException(String.format("로그인이 필요합니다. [%d]", memberId));
        }
    }

    private void validateTheExistenceOfRefreshToken(final Long memberId, final String refreshToken) {
        validateLogged(memberId);
        if (!tokenRepository.existRefreshToken(memberId, refreshToken)) {
            throw new IllegalArgumentException("잘못된 refresh token 입니다.");
        }
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
