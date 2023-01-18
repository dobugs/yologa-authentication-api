package com.dobugs.yologaauthenticationapi.domain;

public enum Provider {

    GOOGLE("google"),
    KAKAO("kakao"),
    ;

    private final String name;

    Provider(final String name) {
        this.name = name;
    }
}
