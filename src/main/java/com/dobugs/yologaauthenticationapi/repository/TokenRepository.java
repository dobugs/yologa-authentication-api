package com.dobugs.yologaauthenticationapi.repository;

import java.util.HashMap;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.dobugs.yologaauthenticationapi.domain.OAuthToken;

@Repository
public class TokenRepository {

    private final HashOperations<String, Object, Object> operations;

    public TokenRepository(final StringRedisTemplate redisTemplate) {
        this.operations = redisTemplate.opsForHash();
    }

    public void save(final OAuthToken oAuthToken) {
        final String key = String.valueOf(oAuthToken.getMemberId());
        final HashMap<String, Object> value = new HashMap<>();
        value.put(oAuthToken.getKeyNameOfProvider(), oAuthToken.getProvider().getName());
        value.put(oAuthToken.getKeyNameOfAccessToken(), oAuthToken.getAccessToken());
        value.put(oAuthToken.getKeyNameOfRefreshToken(), oAuthToken.getRefreshToken());

        operations.putAll(key, value);
    }
}
