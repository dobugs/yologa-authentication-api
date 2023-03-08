package com.dobugs.yologaauthenticationapi.repository;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.dobugs.yologaauthenticationapi.domain.OAuthToken;

@Repository
public class TokenRepository {

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private static final long INFINITE_EXPIRATION = -1L;
    private static final long DELETED = -2L;

    private final StringRedisTemplate redisTemplate;
    private final HashOperations<String, Object, Object> operations;

    public TokenRepository(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.operations = redisTemplate.opsForHash();
    }

    public void save(final OAuthToken oAuthToken) {
        final String key = String.valueOf(oAuthToken.getMemberId());
        final HashMap<String, Object> value = new HashMap<>();
        value.put(OAuthToken.KEY_NAME_OF_PROVIDER, oAuthToken.getProvider().getName());
        value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, oAuthToken.getRefreshToken());

        operations.putAll(key, value);
        redisTemplate.expire(key, oAuthToken.getExpiration(), DEFAULT_TIME_UNIT);
    }

    public void saveRefreshToken(final Long memberId, final String refreshToken) {
        final String key = String.valueOf(memberId);
        final Long expiration = redisTemplate.getExpire(key);

        operations.put(key, OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, refreshToken);
        if (isAbleToReconfigureExpiration(expiration)) {
            redisTemplate.expire(key, expiration, DEFAULT_TIME_UNIT);
        }
    }

    public void delete(final Long memberId) {
        operations.delete(String.valueOf(memberId),
            OAuthToken.KEY_NAME_OF_PROVIDER, OAuthToken.KEY_NAME_OF_REFRESH_TOKEN
        );
    }

    public boolean exist(final Long memberId) {
        final String key = String.valueOf(memberId);
        return operations.hasKey(key, OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);
    }

    public boolean existRefreshToken(final Long memberId, final String refreshToken) {
        final String key = String.valueOf(memberId);
        final String savedRefreshToken = (String) operations.get(key, OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);
        return refreshToken.equals(savedRefreshToken);
    }

    private boolean isAbleToReconfigureExpiration(final Long expiration) {
        return expiration != null && expiration != INFINITE_EXPIRATION && expiration != DELETED;
    }
}
