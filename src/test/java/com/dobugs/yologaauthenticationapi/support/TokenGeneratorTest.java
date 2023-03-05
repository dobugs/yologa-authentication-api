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
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.OAuthTokenResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.ServiceTokenDto;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

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
            final OAuthTokenDto oAuthTokenDto = new OAuthTokenDto(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, EXPIRES_IN, "bearer");

            final ServiceTokenDto serviceTokenDto = tokenGenerator.create(MEMBER_ID, PROVIDER, oAuthTokenDto);

            final Integer memberId = extractMemberId(serviceTokenDto.accessToken());
            final String accessToken = extractToken(serviceTokenDto.accessToken());
            final String refreshToken = extractToken(serviceTokenDto.refreshToken());
            final String provider = extractProvider(serviceTokenDto.accessToken());
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
        private static final Date EXPIRATION = new Date(new Date().getTime() + 10_000);

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

        @DisplayName("잘못된 형식의 JWT 일 경우 예외가 발생한다")
        @Test
        void JWTIsMalformed() {
            final String serviceToken = "malformedToken";

            assertThatThrownBy(() -> tokenGenerator.extract(serviceToken))
                .isInstanceOf(MalformedJwtException.class);
        }

        @DisplayName("지원하지 않는 JWT 일 경우 예외가 발생한다")
        @Test
        void JWTIsUnsupported() {
            final String serviceToken = Jwts.builder()
                .claim("memberId", MEMBER_ID)
                .claim("provider", PROVIDER)
                .claim("token", ACCESS_TOKEN)
                .setExpiration(EXPIRATION)
                .compact();

            assertThatThrownBy(() -> tokenGenerator.extract(serviceToken))
                .isInstanceOf(UnsupportedJwtException.class);
        }

        @DisplayName("서명이 다른 JWT 일 경우 예외가 발생한다")
        @Test
        void signatureIsDifferent() {
            final SecretKey differentSecretKey = Keys.hmacShaKeyFor("differentKey".repeat(10).getBytes(StandardCharsets.UTF_8));
            final String serviceToken = Jwts.builder()
                .claim("memberId", MEMBER_ID)
                .claim("provider", PROVIDER)
                .claim("token", ACCESS_TOKEN)
                .setExpiration(EXPIRATION)
                .signWith(differentSecretKey, SignatureAlgorithm.HS256)
                .compact();

            assertThatThrownBy(() -> tokenGenerator.extract(serviceToken))
                .isInstanceOf(SignatureException.class);
        }

        @DisplayName("만료 시간이 지난 JWT 일 경우 예외가 발생한다")
        @Test
        void JWTIsExpired() {
            final Date expiration = new Date(new Date().getTime() - 1);
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN, expiration);

            assertThatThrownBy(() -> tokenGenerator.extract(serviceToken))
                .isInstanceOf(ExpiredJwtException.class);
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

    @DisplayName("refresh token 만료 시간 설정 테스트")
    @Nested
    public class setUpRefreshTokenExpiration {

        private static final String ACCESS_TOKEN = "accessToken";
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final int EXPIRATION = 10_000;
        private static final String TOKEN_TYPE = "Bearer";

        @DisplayName("refresh token 의 만료 시간이 설정되어 있으면 그대로 반환한다")
        @Test
        void notSetUp() {
            final int refreshTokenExpiresIn = 10_000;
            final OAuthTokenResponse oAuthTokenResponse = new OAuthTokenResponse(ACCESS_TOKEN, EXPIRATION, REFRESH_TOKEN, refreshTokenExpiresIn, TOKEN_TYPE);

            final OAuthTokenDto oAuthTokenDto = tokenGenerator.setUpRefreshTokenExpiration(oAuthTokenResponse);

            assertThat(oAuthTokenDto.refreshTokenExpiresIn()).isEqualTo(refreshTokenExpiresIn);
        }

        @DisplayName("refresh token 이 -1 이면 기본 만료 시간으로 설정한다")
        @Test
        void setUp() {
            final int refreshTokenExpiresIn = -1;
            final OAuthTokenResponse oAuthTokenResponse = new OAuthTokenResponse(ACCESS_TOKEN, EXPIRATION, REFRESH_TOKEN, refreshTokenExpiresIn, TOKEN_TYPE);

            final OAuthTokenDto oAuthTokenDto = tokenGenerator.setUpRefreshTokenExpiration(oAuthTokenResponse);

            assertThat(oAuthTokenDto.refreshTokenExpiresIn()).isEqualTo(DEFAULT_REFRESH_TOKEN_EXPIRES_IN);
        }
    }
}
