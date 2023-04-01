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
import com.dobugs.yologaauthenticationapi.support.fixture.ServiceTokenFixture;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

class TokenGeneratorTest {

    private static final String SECRET_KEY_VALUE = "secretKey".repeat(10);
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_VALUE.getBytes(StandardCharsets.UTF_8));
    private static final long ACCESS_TOKEN_EXPIRES_IN = 1_000_000;
    private static final long REFRESH_TOKEN_EXPIRES_IN = 1_000_000;

    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenGenerator = new TokenGenerator(SECRET_KEY_VALUE, ACCESS_TOKEN_EXPIRES_IN, REFRESH_TOKEN_EXPIRES_IN);
    }

    @DisplayName("token 생성 테스트")
    @Nested
    public class create {

        private static final long MEMBER_ID = 0L;
        private static final String PROVIDER = Provider.GOOGLE.getName();
        private static final String ACCESS_TOKEN = "accessToken";
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final String TOKEN_TYPE = "bearer";
        private static final int EXPIRES_IN = 1_000_000;

        @DisplayName("token 을 생성한다")
        @Test
        void success() {
            final OAuthTokenDto oAuthTokenDto = new OAuthTokenDto(ACCESS_TOKEN, EXPIRES_IN, REFRESH_TOKEN, EXPIRES_IN, TOKEN_TYPE);

            final ServiceTokenDto serviceTokenDto = tokenGenerator.create(MEMBER_ID, PROVIDER, oAuthTokenDto);

            final Integer memberId = ServiceTokenFixture.extractMemberId(serviceTokenDto.accessToken(), SECRET_KEY);
            final String accessToken = ServiceTokenFixture.extractToken(serviceTokenDto.accessToken(), SECRET_KEY);
            final String refreshToken = ServiceTokenFixture.extractToken(serviceTokenDto.refreshToken(), SECRET_KEY);
            final String provider = ServiceTokenFixture.extractProvider(serviceTokenDto.accessToken(), SECRET_KEY);
            final String tokenType = ServiceTokenFixture.extractTokenType(serviceTokenDto.accessToken(), SECRET_KEY);
            assertAll(
                () -> assertThat(memberId).isEqualTo(MEMBER_ID),
                () -> assertThat(accessToken).isEqualTo(ACCESS_TOKEN),
                () -> assertThat(refreshToken).isEqualTo(REFRESH_TOKEN),
                () -> assertThat(provider).isEqualTo(PROVIDER),
                () -> assertThat(tokenType).isEqualTo(TOKEN_TYPE)
            );
        }
    }

    @DisplayName("만료 시간 설정 테스트")
    @Nested
    public class setUpExpiration {

        private static final String ACCESS_TOKEN = "accessToken";
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final int EXPIRATION = 1;
        private static final String TOKEN_TYPE = "Bearer";

        @DisplayName("만료 시간을 설정한다")
        @Test
        void success() {
            final OAuthTokenResponse oAuthTokenResponse = new OAuthTokenResponse(ACCESS_TOKEN, EXPIRATION, REFRESH_TOKEN, EXPIRATION, TOKEN_TYPE);

            final OAuthTokenDto oAuthTokenDto = tokenGenerator.setUpExpiration(oAuthTokenResponse);

            assertAll(
                () -> assertThat(oAuthTokenDto.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRES_IN),
                () -> assertThat(oAuthTokenDto.refreshTokenExpiresIn()).isEqualTo(REFRESH_TOKEN_EXPIRES_IN)
            );
        }
    }
}
