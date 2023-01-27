package com.dobugs.yologaauthenticationapi.domain;

import java.util.Arrays;

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

    public static Provider findOf(final String other) {
        return Arrays.stream(Provider.values())
            .filter(provider -> provider.name.equals(other.toLowerCase()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("잘못된 provider 입니다. [%s]", other)));
    }
}
