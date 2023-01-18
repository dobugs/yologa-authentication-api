package com.dobugs.yologaauthenticationapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthLinkRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.support.GoogleProvider;

@DisplayName("Auth 서비스 테스트")
class AuthServiceTest {

    private static final String YOLOGA_URL = "http://yologa.dobugs.co.kr";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String SCOPE = "scope";
    private static final String AUTH_URL = "authUrl";

    private AuthService authService;

    @BeforeEach
    void setUp() {
        final GoogleProvider googleProvider = new GoogleProvider(CLIENT_ID, CLIENT_SECRET, SCOPE, AUTH_URL);
        authService = new AuthService(googleProvider);
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
    }
}
