package com.dobugs.yologaauthenticationapi.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
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

    public String getKeyNameOfProvider() {
        return "provider";
    }

    public String getKeyNameOfAccessToken() {
        return "accessToken";
    }

    public String getKeyNameOfRefreshToken() {
        return "refreshToken";
    }
}
