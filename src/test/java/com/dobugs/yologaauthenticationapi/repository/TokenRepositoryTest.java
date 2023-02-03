package com.dobugs.yologaauthenticationapi.repository;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.dobugs.yologaauthenticationapi.domain.OAuthToken;
import com.dobugs.yologaauthenticationapi.domain.Provider;

@ActiveProfiles("local")
@DataRedisTest
@DisplayName("Token 레포지토리 테스트")
class TokenRepositoryTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository = new TokenRepository(redisTemplate);
    }

    @DisplayName("OAuthToken 저장 테스트")
    @Nested
    public class save {

        private final long memberId = 0L;

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(memberId));
        }

        @DisplayName("OAuthToken 을 저장한다")
        @Test
        void success() {
            final OAuthToken oAuthToken = OAuthToken.login(memberId, Provider.GOOGLE, "refreshToken");

            assertThatCode(() -> tokenRepository.save(oAuthToken))
                .doesNotThrowAnyException();
        }
    }
}
