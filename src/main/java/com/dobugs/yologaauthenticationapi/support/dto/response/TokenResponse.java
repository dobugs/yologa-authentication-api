package com.dobugs.yologaauthenticationapi.support.dto.response;

public record TokenResponse(String accessToken, int expiresIn, String refreshToken, int refreshTokenExpiresIn, String tokenType) {
}
