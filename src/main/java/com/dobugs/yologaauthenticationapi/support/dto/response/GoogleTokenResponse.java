package com.dobugs.yologaauthenticationapi.support.dto.response;

public record GoogleTokenResponse(String access_token, int expires_in, String token_type, String scope, String refresh_token) {
}
