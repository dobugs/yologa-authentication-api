package com.dobugs.yologaauthenticationapi.config.dto.response;

public record ServiceToken(Long memberId, String provider, String tokenType, String token) {
}
