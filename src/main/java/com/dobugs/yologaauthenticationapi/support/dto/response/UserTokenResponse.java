package com.dobugs.yologaauthenticationapi.support.dto.response;

public record UserTokenResponse(Long memberId, String provider, String tokenType, String token) {
}
