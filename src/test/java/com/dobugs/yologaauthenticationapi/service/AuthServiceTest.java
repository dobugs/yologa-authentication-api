package com.dobugs.yologaauthenticationapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthLinkRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.support.OAuthProvider;

@DisplayName("Auth 서비스 테스트")
class AuthServiceTest {

    private static final String YOLOGA_URL = "http://yologa.dobugs.co.kr";

    private AuthService authService;

    @BeforeEach
    void setUp() {
        final OAuthProvider provider = new FakeProvider();
        authService = new AuthService(provider, provider);
    }

    @DisplayName("OAuth URL 생성 테스트")
    @Nested
    public class generateOAuthUrl {

        @DisplayName("구글 OAuth URL 을 생성한다")
        @Test
        void generateGoogleOAuthUrl() {
            final String provider = "google";
            final OAuthLinkRequest request = new OAuthLinkRequest(provider, YOLOGA_URL);

            final OAuthLinkResponse response = authService.generateOAuthUrl(request);

            assertThat(response.oauthLoginLink()).contains(YOLOGA_URL);
        }

        @DisplayName("카카오 OAuth URL 을 생성한다")
        @Test
        void generateKakaoOAuthUrl() {
            final String provider = "kakao";
            final OAuthLinkRequest request = new OAuthLinkRequest(provider, YOLOGA_URL);

            final OAuthLinkResponse response = authService.generateOAuthUrl(request);

            assertThat(response.oauthLoginLink()).contains(YOLOGA_URL);
        }

        @DisplayName("존재하지 않는 provider 를 요청할 경우 예외가 발생한다")
        @Test
        void notExistProvider() {
            final String provider = "notExistProvider";
            final OAuthLinkRequest request = new OAuthLinkRequest(provider, YOLOGA_URL);

            assertThatThrownBy(() -> authService.generateOAuthUrl(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 provider 입니다.");
        }
    }
}
