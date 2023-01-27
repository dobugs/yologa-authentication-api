package com.dobugs.yologaauthenticationapi.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RedisHash
public class OAuthToken {

    @Id
    private Long memberId;

    private Provider provider;
    private String accessToken;
    private String refreshToken;

    public static OAuthToken login(final Long memberId, final Provider provider, final String refreshToken) {
        return new OAuthToken(memberId, provider, null, refreshToken);
    }
}
