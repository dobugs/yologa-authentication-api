package com.dobugs.yologaauthenticationapi.support.dto.response;

public record OAuthTokenDto(String accessToken, long expiresIn, String refreshToken, long refreshTokenExpiresIn, String tokenType) {
}
