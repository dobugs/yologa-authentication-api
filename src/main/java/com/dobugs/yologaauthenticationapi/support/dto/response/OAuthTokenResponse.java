package com.dobugs.yologaauthenticationapi.support.dto.response;

public record OAuthTokenResponse(String accessToken, int expiresIn, String refreshToken, int refreshTokenExpiresIn, String tokenType) {
}
