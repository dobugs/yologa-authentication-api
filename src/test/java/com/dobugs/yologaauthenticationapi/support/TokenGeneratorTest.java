package com.dobugs.yologaauthenticationapi.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.support.dto.response.TokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
        private static final String PROVIDER = Provider.GOOGLE.getName();
        private static final String ACCESS_TOKEN = "accessToken";
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final int EXPIRES_IN = 1000;

        @DisplayName("token 을 생성한다")
        @Test
        void success() {
            final TokenResponse tokenResponse = new TokenResponse(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, EXPIRES_IN, "bearer");

            final ServiceTokenResponse serviceTokenResponse = tokenGenerator.create(MEMBER_ID, PROVIDER, tokenResponse);

            final Integer memberId = extractMemberId(serviceTokenResponse.accessToken());
            final String accessToken = extractToken(serviceTokenResponse.accessToken());
            final String refreshToken = extractToken(serviceTokenResponse.refreshToken());
            final String provider = extractProvider(serviceTokenResponse.accessToken());
            assertAll(
                () -> assertThat(memberId).isEqualTo(MEMBER_ID),
                () -> assertThat(accessToken).isEqualTo(ACCESS_TOKEN),
                () -> assertThat(refreshToken).isEqualTo(REFRESH_TOKEN),
                () -> assertThat(provider).isEqualTo(PROVIDER)
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

        private String extractProvider(final String createdToken) {
            return (String) Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(createdToken)
                .getBody()
                .get("provider");
        }
    }

    @DisplayName("토큰 추출 테스트")
    @Nested
    public class extract {

        private static final long MEMBER_ID = 0L;
        private static final String PROVIDER = Provider.GOOGLE.getName();
        private static final String ACCESS_TOKEN = "accessToken";
        private static final Date EXPIRATION = new Date(new Date().getTime() + 1000);

        @DisplayName("token 을 추출한다")
        @Test
        void success() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN, EXPIRATION);

            final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);

            assertAll(
                () -> assertThat(userTokenResponse.memberId()).isEqualTo(MEMBER_ID),
                () -> assertThat(userTokenResponse.token()).isEqualTo(ACCESS_TOKEN),
                () -> assertThat(userTokenResponse.provider()).isEqualTo(PROVIDER)
            );
        }

        @DisplayName("만료 시간이 지난 token 일 경우 예외가 발생한다")
        @Test
        void fail() {
            final Date expiration = new Date(new Date().getTime() - 1);
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN, expiration);

            assertThatThrownBy(() -> tokenGenerator.extract(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("토큰의 만료 시간이 지났습니다.");
        }

        private String createToken(final Long memberId, final String provider, final String token, final Date expiration) {
            return Jwts.builder()
                .claim("memberId", memberId)
                .claim("provider", provider)
                .claim("token", token)
                .setExpiration(expiration)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        }
    }
}