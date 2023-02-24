package com.dobugs.yologaauthenticationapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.FakeOAuthConnector;
import com.dobugs.yologaauthenticationapi.support.OAuthConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenResponse;
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
        final OAuthConnector connector = new FakeOAuthConnector();
        authService = new AuthService(connector, connector, memberRepository, tokenRepository, tokenGenerator);
    }

    @DisplayName("OAuth URL 생성 테스트")
    @Nested
    public class generateOAuthTokenUrl {

        @DisplayName("OAuth URL 을 생성한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "kakao"})
        void success(final String provider) {
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

    @DisplayName("로그인 테스트")
    @Nested
    public class login {

        private static final String ACCESS_TOKEN = "accessToken";
        private static final String REFRESH_TOKEN = "refreshToken";

        @DisplayName("로그인한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "kakao"})
        void success(final String provider) {
            final OAuthRequest request = new OAuthRequest(provider, REDIRECT_URL, REFERRER_URL);
            final OAuthCodeRequest codeRequest = new OAuthCodeRequest("authorizationCode");

            given(memberRepository.findByOauthId(any())).willReturn(Optional.of(new Member("oauthId")));
            given(tokenGenerator.create(any(), eq(provider), any())).willReturn(new ServiceTokenResponse(ACCESS_TOKEN, REFRESH_TOKEN));

            final OAuthTokenResponse response = authService.login(request, codeRequest);

            assertAll(
                () -> assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN),
                () -> assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN)
            );
        }

        @DisplayName("존재하지 않는 provider 를 요청할 경우 예외가 발생한다")
        @Test
        void notExistProvider() {
            final String provider = "notExistProvider";
            final OAuthRequest request = new OAuthRequest(provider, REDIRECT_URL, REFERRER_URL);
            final OAuthCodeRequest codeRequest = new OAuthCodeRequest("authorizationCode");

            assertThatThrownBy(() -> authService.login(request, codeRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 provider 입니다.");
        }

        @DisplayName("처음 로그인한 사용자는 사용자 정보를 저장 후 로그인한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "kakao"})
        void register(final String provider) {
            final OAuthRequest request = new OAuthRequest(provider, REDIRECT_URL, REFERRER_URL);
            final OAuthCodeRequest codeRequest = new OAuthCodeRequest("authorizationCode");

            given(memberRepository.findByOauthId(any())).willReturn(Optional.empty());
            given(tokenGenerator.create(any(), eq(provider), any())).willReturn(new ServiceTokenResponse(ACCESS_TOKEN, REFRESH_TOKEN));

            final OAuthTokenResponse response = authService.login(request, codeRequest);

            assertAll(
                () -> assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN),
                () -> assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN)
            );
        }
    }

    @DisplayName("Access Token 재발급 테스트")
    @Nested
    public class reissue {

        @DisplayName("Access Token 을 재발급한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "kakao"})
        void success(final String provider) {
            final long memberId = 0L;
            final String refreshToken = "refreshToken";
            final String serviceToken = createToken(memberId, provider, refreshToken);

            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(memberId, provider, refreshToken));
            given(tokenRepository.exist(memberId)).willReturn(true);
            given(tokenRepository.existRefreshToken(memberId, refreshToken)).willReturn(true);
            given(tokenGenerator.create(eq(memberId), eq(provider), any())).willReturn(new ServiceTokenResponse("accessToken", "refreshToken"));

            assertThatCode(() -> authService.reissue(serviceToken))
                .doesNotThrowAnyException();
        }

        @DisplayName("존재하지 않는 provider 를 요청할 경우 예외가 발생한다")
        @Test
        void notExistProvider() {
            final long memberId = 0L;
            final String provider = "notExistProvider";
            final String refreshToken = "refreshToken";
            final String serviceToken = createToken(memberId, provider, refreshToken);

            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(memberId, provider, refreshToken));

            assertThatThrownBy(() -> authService.reissue(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 provider 입니다.");
        }

        @DisplayName("memberId 가 존재하지 않을 경우 예외가 발생한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "kakao"})
        void notExistMemberId(final String provider) {
            final long notExistMemberId = 0L;
            final String existRefreshToken = "refreshToken";
            final String serviceToken = createToken(notExistMemberId, provider, existRefreshToken);

            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(notExistMemberId, provider, existRefreshToken));
            given(tokenRepository.exist(notExistMemberId)).willReturn(false);

            assertThatThrownBy(() -> authService.reissue(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("로그인이 필요합니다.");
        }

        @DisplayName("refresh token 이 일치하지 않을 경우 예외가 발생한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "kakao"})
        void notEqualsRefreshToken(final String provider) {
            final long existMemberId = 0L;
            final String notExistRefreshToken = "refreshToken";
            final String serviceToken = createToken(existMemberId, provider, notExistRefreshToken);

            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(existMemberId, provider, notExistRefreshToken));
            given(tokenRepository.exist(existMemberId)).willReturn(true);
            given(tokenRepository.existRefreshToken(existMemberId, notExistRefreshToken)).willReturn(false);

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

    @DisplayName("로그아웃 테스트")
    @Nested
    public class logout {

        @DisplayName("로그아웃한다")
        @Test
        void success() {
            final long memberId = 0L;
            final String provider = "google";
            final String refreshToken = "refreshToken";
            final String serviceToken = createToken(memberId, provider, refreshToken);

            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(memberId, provider, refreshToken));
            given(tokenRepository.exist(memberId)).willReturn(true);

            assertThatCode(() -> authService.logout(serviceToken))
                .doesNotThrowAnyException();
        }

        @DisplayName("memberId 가 존재하지 않을 경우 예외가 발생한다")
        @Test
        void notExistMemberId() {
            final long notExistMemberId = 0L;
            final String provider = "google";
            final String refreshToken = "refreshToken";
            final String serviceToken = createToken(notExistMemberId, provider, refreshToken);

            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(notExistMemberId, provider, refreshToken));
            given(tokenRepository.exist(notExistMemberId)).willReturn(false);

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
