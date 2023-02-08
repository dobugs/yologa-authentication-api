package com.dobugs.yologaauthenticationapi.support.dto.response;

public record GoogleAccessTokenResponse(String access_token, int expires_in, String scope, String token_type) {
}
