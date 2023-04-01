package com.dobugs.yologaauthenticationapi.support.dto.response;

public record KakaoTokenResponse(String token_type, String access_token, int expires_in, String refresh_token, int refresh_token_expires_in) {
}
