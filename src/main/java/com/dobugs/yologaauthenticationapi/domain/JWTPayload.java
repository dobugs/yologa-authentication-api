package com.dobugs.yologaauthenticationapi.domain;

import lombok.Getter;

@Getter
public enum JWTPayload {

    MEMBER_ID("memberId"),
    PROVIDER("provider"),
    TOKEN_TYPE("tokenType"),
    TOKEN("token"),
    ;

    private final String name;

    JWTPayload(final String name) {
        this.name = name;
    }
}
