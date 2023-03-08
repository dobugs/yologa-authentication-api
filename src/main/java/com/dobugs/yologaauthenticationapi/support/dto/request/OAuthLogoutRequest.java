package com.dobugs.yologaauthenticationapi.support.dto.request;

public record OAuthLogoutRequest(String accessToken, String refreshToken, String tokenType) {
}
