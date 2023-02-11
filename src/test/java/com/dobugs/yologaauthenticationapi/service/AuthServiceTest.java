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

        private static final String PROVIDER = "google";

        @DisplayName("memberId 가 존재하지 않을 경우 예외가 발생한다")
        @Test
        void notExistMemberId() {
            final long notExistMemberId = 0L;
            final String existRefreshToken = "refreshToken";
            final String serviceToken = createToken(notExistMemberId, PROVIDER, existRefreshToken);

            given(tokenGenerator.extract(serviceToken))
                .willReturn(new UserTokenResponse(notExistMemberId, PROVIDER, existRefreshToken));
            given(tokenRepository.exist(notExistMemberId))
                .willReturn(false);

            assertThatThrownBy(() -> authService.reissue(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("로그인이 필요합니다.");
        }

        @DisplayName("refresh token 이 일치하지 않을 경우 예외가 발생한다")
        @Test
        void notEqualsRefreshToken() {
            final long existMemberId = 0L;
            final String notExistRefreshToken = "refreshToken";
            final String serviceToken = createToken(existMemberId, PROVIDER, notExistRefreshToken);

            given(tokenGenerator.extract(serviceToken))
                .willReturn(new UserTokenResponse(existMemberId, PROVIDER, notExistRefreshToken));
            given(tokenRepository.exist(existMemberId))
                .willReturn(true);
            given(tokenRepository.existRefreshToken(existMemberId, notExistRefreshToken))
                .willReturn(false);

            assertThatThrownBy(() -> authService.reissue(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 refresh token 입니다.");
        }

        private String createToken(final Long memberId, final String provider, final String token) {
            return Jwts.builder()
                .claim("memberId", memberId)
                .claim("provider", provider)
                .claim("token", token)
                .compact();
        }
    }

    @DisplayName("로그아웃 시 로그인 여부 검증 테스트")
    @Nested
    public class validateLogged {

        @DisplayName("memberId 가 존재하지 않을 경우 예외가 발생한다")
        @Test
        void notExistMemberId() {
            final long notExistMemberId = 0L;
            final String provider = "google";
            final String refreshToken = "refreshToken";
            final String serviceToken = createToken(notExistMemberId, provider, refreshToken);

            given(tokenGenerator.extract(serviceToken))
                .willReturn(new UserTokenResponse(notExistMemberId, provider, refreshToken));
            given(tokenRepository.exist(notExistMemberId))
                .willReturn(false);

            assertThatThrownBy(() -> authService.logout(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("로그인이 필요합니다.");
        }

        private String createToken(final Long memberId, final String provider, final String token) {
            return Jwts.builder()
                .claim("memberId", memberId)
                .claim("provider", provider)
                .claim("token", token)
                .compact();
        }
    }
}
