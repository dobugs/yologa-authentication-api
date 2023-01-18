package com.dobugs.yologaauthenticationapi.service.dto.request;

public record OAuthLinkRequest(String provider, String redirect_url) {
}
