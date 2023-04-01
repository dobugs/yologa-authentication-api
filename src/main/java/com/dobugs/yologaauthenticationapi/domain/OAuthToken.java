package com.dobugs.yologaauthenticationapi.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RedisHash
public class OAuthToken {

    public static final String KEY_NAME_OF_PROVIDER = "provider";
    public static final String KEY_NAME_OF_REFRESH_TOKEN = "refreshToken";

    @Id
    private Long memberId;

    private Provider provider;
    private String accessToken;
    private String refreshToken;
    private Long expiration;

    public static OAuthToken login(final Long memberId, final Provider provider, final String refreshToken, final Long expiration) {
        return new OAuthToken(memberId, provider, null, refreshToken, expiration);
    }
}
