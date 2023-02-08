package com.dobugs.yologaauthenticationapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.HashOperations;
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

        private static final long MEMBER_ID = 0L;

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(MEMBER_ID));
        }

        @DisplayName("OAuthToken 을 저장한다")
        @Test
        void success() {
            final OAuthToken oAuthToken = OAuthToken.login(MEMBER_ID, Provider.GOOGLE, "refreshToken");

            assertThatCode(() -> tokenRepository.save(oAuthToken))
                .doesNotThrowAnyException();
        }
    }

    @DisplayName("refresh token 존재 여부 테스트")
    @Nested
    public class existRefreshToken {

        private static final long EXIST_MEMBER_ID = 0L;
        private static final String EXIST_REFRESH_TOKEN = "refreshToken";

        @BeforeEach
        void setUp() {
            final HashMap<String, Object> value = new HashMap<>();
            value.put(OAuthToken.KEY_NAME_OF_PROVIDER, Provider.GOOGLE.getName());
            value.put(OAuthToken.KEY_NAME_OF_ACCESS_TOKEN, null);
            value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, EXIST_REFRESH_TOKEN);

            final HashOperations<String, Object, Object> operations = redisTemplate.opsForHash();
            operations.putAll(String.valueOf(EXIST_MEMBER_ID), value);
        }

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(EXIST_MEMBER_ID));
        }

        @DisplayName("member id 에 해당하는 refresh token 이 존재할 경우 true 를 반환한다")
        @Test
        void exist() {
            final boolean existRefreshToken = tokenRepository.existRefreshToken(EXIST_MEMBER_ID, EXIST_REFRESH_TOKEN);

            assertThat(existRefreshToken).isTrue();
        }

        @DisplayName("member id 가 존재하지 않을 경우 false 를 반환한다")
        @Test
        void notExistMemberId() {
            final long notExistMemberId = -1L;

            final boolean existRefreshToken = tokenRepository.existRefreshToken(notExistMemberId, EXIST_REFRESH_TOKEN);

            assertThat(existRefreshToken).isFalse();
        }

        @DisplayName("refresh token 이 동일하지 않은 경우 false 를 반환한다")
        @Test
        void notEqualsRefreshToken() {
            final String notExistRefreshToken = "otherRefreshToken";

            final boolean existRefreshToken = tokenRepository.existRefreshToken(EXIST_MEMBER_ID, notExistRefreshToken);

            assertThat(existRefreshToken).isFalse();
        }
    }
}
