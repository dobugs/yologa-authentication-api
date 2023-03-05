package com.dobugs.yologaauthenticationapi.support.dto.response;

public record OAuthTokenDto(String accessToken, int expiresIn, String refreshToken, int refreshTokenExpiresIn, String tokenType) {
}
