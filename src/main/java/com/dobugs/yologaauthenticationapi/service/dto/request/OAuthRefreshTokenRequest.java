package com.dobugs.yologaauthenticationapi.service.dto.request;

public record OAuthRefreshTokenRequest(Long memberId, String refreshToken) {
}
