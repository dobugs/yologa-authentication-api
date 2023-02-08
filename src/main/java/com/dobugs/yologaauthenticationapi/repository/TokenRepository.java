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
        value.put(OAuthToken.KEY_NAME_OF_PROVIDER, oAuthToken.getProvider().getName());
        value.put(OAuthToken.KEY_NAME_OF_ACCESS_TOKEN, oAuthToken.getAccessToken());
        value.put(OAuthToken.KEY_NAME_OF_REFRESH_TOKEN, oAuthToken.getRefreshToken());

        operations.putAll(key, value);
    }

    public boolean existRefreshToken(final Long memberId, final String refreshToken) {
        final String key = String.valueOf(memberId);
        final String savedRefreshToken = (String) operations.get(key, OAuthToken.KEY_NAME_OF_REFRESH_TOKEN);
        return refreshToken.equals(savedRefreshToken);
    }
}
