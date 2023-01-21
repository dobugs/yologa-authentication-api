package com.dobugs.yologaauthenticationapi.domain;

import lombok.Getter;

@Getter
public enum Provider {

    GOOGLE("google"),
    KAKAO("kakao"),
    ;

    private final String name;

    Provider(final String name) {
        this.name = name;
    }
}
