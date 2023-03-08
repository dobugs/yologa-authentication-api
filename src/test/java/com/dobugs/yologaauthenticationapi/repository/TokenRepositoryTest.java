package com.dobugs.yologaauthenticationapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
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

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(MEMBER_ID));
        }

        @DisplayName("OAuthToken 을 저장한다")
        @Test
        void success() {
            final OAuthToken oAuthToken = OAuthToken.login(MEMBER_ID, Provider.GOOGLE, "refreshToken", 1L);

            assertThatCode(() -> tokenRepository.save(oAuthToken))
                .doesNotThrowAnyException();
        }

        @DisplayName("저장한 OAuthToken 은 만료 시간 이후에 자동 제거된다")
        @Test
        void expire() throws InterruptedException {
            final long expiration = 1L;

            final OAuthToken oAuthToken = OAuthToken.login(MEMBER_ID, Provider.GOOGLE, "refreshToken", expiration);
            tokenRepository.save(oAuthToken);
            TimeUnit.SECONDS.sleep(expiration);

            final Boolean hasKey = operations.hasKey(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);

            assertThat(hasKey).isFalse();
        }
    }

    @DisplayName("refresh token 재저장 테스트")
    @Nested
    public class saveRefreshToken {

        private static final long MEMBER_ID = 0L;
        private static final String REFRESH_TOKEN = "refresh token";

        @AfterEach
        void tearDown() {
            redisTemplate.delete(String.valueOf(MEMBER_ID));
        }

        @DisplayName("이전에 저장되어 있던 refresh token 을 덮어서 저장한다")
        @Test
        void restore() {
            final HashMap<String, Object> value = new HashMap<>();
            value.put(OAuthToken.KEY_NAME_OF_PROVIDER, Provider.GOOGLE.getName());
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
            final long expected = 10L;
            operations.put(String.valueOf(MEMBER_ID), OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, "beforeRefreshToken");
            redisTemplate.expire(String.valueOf(MEMBER_ID), expected, TimeUnit.SECONDS);

            tokenRepository.saveRefreshToken(MEMBER_ID, REFRESH_TOKEN);

            final Long actual = redisTemplate.getExpire(String.valueOf(MEMBER_ID));
            assertThat(actual)
                .isLessThanOrEqualTo(expected)
                .isGreaterThan(0L);
        }
    }

    @DisplayName("삭제 테스트")
    @Nested
    public class delete {

        private static final long MEMBER_ID = 0L;

        @BeforeEach
        void setUp() {
            final HashMap<String, Object> value = new HashMap<>();
            value.put(OAuthToken.KEY_NAME_OF_PROVIDER, Provider.GOOGLE.getName());
            value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, "refreshToken");

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

    @DisplayName("사용자 존재 여부 테스트")
    @Nested
    public class exist {

        private static final long EXIST_MEMBER_ID = 0L;
        private static final String EXIST_REFRESH_TOKEN = "refreshToken";

        @BeforeEach
        void setUp() {
            final HashMap<String, Object> value = new HashMap<>();
            value.put(OAuthToken.KEY_NAME_OF_PROVIDER, Provider.GOOGLE.getName());
            value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, EXIST_REFRESH_TOKEN);

            final HashOperations<String, Object, Object> operations = redisTemplate.opsForHash();
            operations.putAll(String.valueOf(EXIST_MEMBER_ID), value);
        }

        @DisplayName("member id 에 해당하는 정보가 존재할 경우 true 를 반환한다")
        @Test
        void existMemberId() {
            final boolean exist = tokenRepository.exist(EXIST_MEMBER_ID);

            assertThat(exist).isTrue();
        }

        @DisplayName("member id 에 해당하는 정보가 존재하지 않을 경우 false 를 반환한다")
        @Test
        void notExistMemberId() {
            final long notExistMemberId = -1L;

            final boolean exist = tokenRepository.exist(notExistMemberId);

            assertThat(exist).isFalse();
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
