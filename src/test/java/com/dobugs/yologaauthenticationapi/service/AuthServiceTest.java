package com.dobugs.yologaauthenticationapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthProviderRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth 서비스 테스트")
class AuthServiceTest {

    private static final String REDIRECT_URL = "https://yologa.dobugs.co.kr";
    private static final String REFERRER_URL = "https://yologa.dobugs.co.kr";

    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        final OAuthConnector connector = new FakeConnector();
        authService = new AuthService(connector, connector, memberRepository, tokenRepository, tokenGenerator);
    }

    @DisplayName("OAuth URL 생성 테스트")
    @Nested
    public class generateOAuthTokenUrl {

        @DisplayName("구글 OAuth URL 을 생성한다")
        @Test
        void generateGoogleOAuthUrl() {
            final String provider = "google";
            final OAuthRequest request = new OAuthRequest(provider, REDIRECT_URL, REFERRER_URL);

            final OAuthLinkResponse response = authService.generateOAuthUrl(request);

            assertThat(response.oauthLoginLink()).contains(REDIRECT_URL);
        }

        @DisplayName("카카오 OAuth URL 을 생성한다")
        @Test
        void generateKakaoOAuthUrl() {
            final String provider = "kakao";
            final OAuthRequest request = new OAuthRequest(provider, REDIRECT_URL, REFERRER_URL);

            final OAuthLinkResponse response = authService.generateOAuthUrl(request);

            assertThat(response.oauthLoginLink()).contains(REDIRECT_URL);
        }

        @DisplayName("존재하지 않는 provider 를 요청할 경우 예외가 발생한다")
        @Test
        void notExistProvider() {
            final String provider = "notExistProvider";
            final OAuthRequest request = new OAuthRequest(provider, REDIRECT_URL, REFERRER_URL);

            assertThatThrownBy(() -> authService.generateOAuthUrl(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 provider 입니다.");
        }
    }

    @DisplayName("Access Token 재발급 시 Refresh Token 검증 테스트")
    @Nested
    public class validateTheExistenceOfRefreshToken {

        @DisplayName("memberId 가 존재하지 않을 경우 예외가 발생한다")
        @Test
        void notExistMemberId() {
            final long notExistMemberId = 0L;
            final String existRefreshToken = "refreshToken";
            final String serviceToken = createToken(notExistMemberId, existRefreshToken);

            final OAuthProviderRequest request = new OAuthProviderRequest("google");

            given(tokenGenerator.extract(serviceToken))
                .willReturn(new UserTokenResponse(notExistMemberId, existRefreshToken));
            given(tokenRepository.existRefreshToken(notExistMemberId, existRefreshToken))
                .willReturn(false);

            assertThatThrownBy(() -> authService.reissue(request, serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 refresh token 입니다.");
        }

        @DisplayName("refresh token 이 일치하지 않을 경우 예외가 발생한다")
        @Test
        void notEqualsRefreshToken() {
            final long existMemberId = 0L;
            final String notExistRefreshToken = "refreshToken";
            final String serviceToken = createToken(existMemberId, notExistRefreshToken);

            final OAuthProviderRequest request = new OAuthProviderRequest("google");

            given(tokenGenerator.extract(serviceToken))
                .willReturn(new UserTokenResponse(existMemberId, notExistRefreshToken));
            given(tokenRepository.existRefreshToken(existMemberId, notExistRefreshToken))
                .willReturn(false);

            assertThatThrownBy(() -> authService.reissue(request, serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 refresh token 입니다.");
        }

        private String createToken(final Long memberId, final String token) {
            return Jwts.builder()
                .claim("memberId", memberId)
                .claim("token", token)
                .compact();
        }
    }
}
