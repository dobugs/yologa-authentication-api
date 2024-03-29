package com.dobugs.yologaauthenticationapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    @Autowired
    private StringRedisTemplate redisTemplate;
    private HashOperations<String, Object, Object> operations;

    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        operations = redisTemplate.opsForHash();
        tokenRepository = new TokenRepository(redisTemplate);
    }

    @DisplayName("OAuthToken 저장 테스트")
    @Nested
    public class save {

        private static final long MEMBER_ID = 0L;
        private static final Provider PROVIDER = Provider.GOOGLE;
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final long EXPIRATION = 1_000L;

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(MEMBER_ID));
        }

        @DisplayName("OAuthToken 을 저장한다")
        @Test
        void success() {
            final OAuthToken oAuthToken = OAuthToken.login(MEMBER_ID, PROVIDER, REFRESH_TOKEN, EXPIRATION);

            tokenRepository.save(oAuthToken);

            final String key = String.valueOf(MEMBER_ID);
            assertAll(
                () -> assertThat(operations.get(key, OAuthToken.KEY_NAME_OF_PROVIDER)).isEqualTo(PROVIDER.getName()),
                () -> assertThat(operations.get(key, OAuthToken.KEY_NAME_OF_REFRESH_TOKEN)).isEqualTo(REFRESH_TOKEN)
            );
        }

        @DisplayName("재저장 시 만료 시간을 재설정한다")
        @Test
        void alreadySaved() {
            final long restoredExpiration = EXPIRATION * 100;

            operations.put(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, REFRESH_TOKEN);
            redisTemplate.expire(String.valueOf(MEMBER_ID), EXPIRATION, TIME_UNIT);

            final OAuthToken oAuthToken = OAuthToken.login(MEMBER_ID, PROVIDER, REFRESH_TOKEN, restoredExpiration);
            tokenRepository.save(oAuthToken);

            final Long savedExpiration = redisTemplate.getExpire(String.valueOf(MEMBER_ID), TIME_UNIT);
            assertThat(savedExpiration).isGreaterThan(EXPIRATION);
        }

        @DisplayName("저장한 OAuthToken 은 만료 시간 이후에 자동 제거된다")
        @Test
        void expire() throws InterruptedException {
            final OAuthToken oAuthToken = OAuthToken.login(MEMBER_ID, PROVIDER, REFRESH_TOKEN, EXPIRATION);
            tokenRepository.save(oAuthToken);
            TIME_UNIT.sleep(EXPIRATION);

            final Boolean hasKey = operations.hasKey(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);

            assertThat(hasKey).isFalse();
        }
    }

    @DisplayName("refresh token 재저장 테스트")
    @Nested
    public class saveRefreshToken {

        private static final long MEMBER_ID = 0L;
        private static final Provider PROVIDER = Provider.GOOGLE;
        private static final String REFRESH_TOKEN = "refresh token";

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(MEMBER_ID));
        }

        @DisplayName("이전에 저장되어 있던 refresh token 을 덮어서 저장한다")
        @Test
        void restore() {
            final HashMap<String, Object> value = new HashMap<>();
            value.put(OAuthToken.KEY_NAME_OF_PROVIDER, PROVIDER.getName());
            value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, "beforeRefreshToken");
            operations.putAll(String.valueOf(MEMBER_ID), value);

            tokenRepository.saveRefreshToken(MEMBER_ID, REFRESH_TOKEN);

            final String savedRefreshToken = (String) operations.get(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);

            assertThat(savedRefreshToken).isEqualTo(REFRESH_TOKEN);
        }

        @DisplayName("이전에 refresh token 이 없는 상태에서 저장한다")
        @Test
        void save() {
            tokenRepository.saveRefreshToken(MEMBER_ID, REFRESH_TOKEN);

            final String savedRefreshToken = (String) operations.get(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);

            assertThat(savedRefreshToken).isEqualTo(REFRESH_TOKEN);
        }

        @DisplayName("재저장한 refresh token 은 이전에 설정되어 있던 만료 시간으로 재설정된다")
        @Test
        void expire() {
            final long expected = 1_000L * 10;
            operations.put(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, "beforeRefreshToken");
            redisTemplate.expire(String.valueOf(MEMBER_ID), expected, TIME_UNIT);

            tokenRepository.saveRefreshToken(MEMBER_ID, REFRESH_TOKEN);

            final Long actual = redisTemplate.getExpire(String.valueOf(MEMBER_ID), TIME_UNIT);
            assertThat(actual)
                .isLessThanOrEqualTo(expected)
                .isGreaterThan(0L);
        }
    }

    @DisplayName("Refresh Token 조회 테스트")
    @Nested
    public class findRefreshToken {

        private static final long MEMBER_ID = 0L;
        private static final String REFRESH_TOKEN = "refreshToken";
        private static final long EXPIRATION = 1_000L * 10;

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(MEMBER_ID));
        }

        @DisplayName("Member ID 에 저장되어 있는 Refresh Token 을 조회한다")
        @Test
        void success() {
            operations.put(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, REFRESH_TOKEN);
            redisTemplate.expire(String.valueOf(MEMBER_ID), EXPIRATION, TIME_UNIT);

            final Optional<String> refreshToken = tokenRepository.findRefreshToken(MEMBER_ID);

            assertThat(refreshToken).isPresent();
        }

        @DisplayName("Member ID 에 저장되어 있는 Refresh Token 이 없을 경우 빈값을 반환한다")
        @Test
        void notExist() {
            final Optional<String> refreshToken = tokenRepository.findRefreshToken(MEMBER_ID);

            assertThat(refreshToken).isEmpty();
        }
    }

    @DisplayName("삭제 테스트")
    @Nested
    public class delete {

        private static final long MEMBER_ID = 0L;
        private static final Provider PROVIDER = Provider.GOOGLE;
        private static final String REFRESH_TOKEN = "refresh token";

        @BeforeEach
        void setUp() {
            final HashMap<String, Object> value = new HashMap<>();
            value.put(OAuthToken.KEY_NAME_OF_PROVIDER, PROVIDER.getName());
            value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, REFRESH_TOKEN);

            final HashOperations<String, Object, Object> operations = redisTemplate.opsForHash();
            operations.putAll(String.valueOf(MEMBER_ID), value);
        }

        @DisplayName("key 에 해당하는 모든 정보를 제거한다")
        @Test
        void deleteValue() {
            tokenRepository.delete(MEMBER_ID);

            final Boolean hasProvider = operations.hasKey(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_PROVIDER);
            final Boolean hasRefreshToken = operations.hasKey(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);
            assertAll(
                () -> assertThat(hasProvider).isFalse(),
                () -> assertThat(hasRefreshToken).isFalse()
            );
        }
    }
}
