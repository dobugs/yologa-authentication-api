package com.dobugs.yologaauthenticationapi.auth.dto.response;

public record ServiceToken(Long memberId, String provider, String tokenType, String token) {
}
