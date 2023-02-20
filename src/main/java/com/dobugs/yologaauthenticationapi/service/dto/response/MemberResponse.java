package com.dobugs.yologaauthenticationapi.service.dto.response;

public record MemberResponse(Long id, String oauthId, String nickname, String phoneNumber, String profileUrl) {
}
