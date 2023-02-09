package com.dobugs.yologaauthenticationapi.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class TokenGeneratorTest {

    private static final String SECRET_KEY_VALUE = "secretKey".repeat(10);
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_VALUE.getBytes(StandardCharsets.UTF_8));
    private static final int DEFAULT_REFRESH_TOKEN_EXPIRES_IN = 1000;

    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenGenerator = new TokenGenerator(SECRET_KEY_VALUE, DEFAULT_REFRESH_TOKEN_EXPIRES_IN);
    }

    @DisplayName("token 생성 테스트")
    @Nested
    public class create {

        private static final long MEMBER_ID = 0L;
        private static final String ACCESS_TOKEN = "accessToken";
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final int EXPIRES_IN = 1000;

        @DisplayName("token 을 생성한다")
        @Test
        void success() {
            final TokenResponse tokenResponse = new TokenResponse(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, EXPIRES_IN, "bearer");

            final ServiceTokenResponse serviceTokenResponse = tokenGenerator.create(MEMBER_ID, tokenResponse);

            final Integer memberId = extractMemberId(serviceTokenResponse.accessToken());
            final String accessToken = extractToken(serviceTokenResponse.accessToken());
            final String refreshToken = extractToken(serviceTokenResponse.refreshToken());
            assertAll(
                () -> assertThat(memberId).isEqualTo(MEMBER_ID),
                () -> assertThat(accessToken).isEqualTo(ACCESS_TOKEN),
                () -> assertThat(refreshToken).isEqualTo(REFRESH_TOKEN)
            );
        }

        private Integer extractMemberId(final String createdToken) {
            return (Integer) Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(createdToken)
                .getBody()
                .get("memberId");
        }

        private String extractToken(final String createdToken) {
            return (String) Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(createdToken)
                .getBody()
                .get("token");
        }
    }
}
