package com.dobugs.yologaauthenticationapi.service.dto.request;

public record OAuthRequest(String provider, String redirect_url, String referrer) {
}
