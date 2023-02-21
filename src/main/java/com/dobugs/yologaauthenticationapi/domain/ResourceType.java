package com.dobugs.yologaauthenticationapi.domain;

public enum ResourceType {

    PROFILE("profile_image"),
    ;

    private final String name;

    ResourceType(final String name) {
        this.name = name;
    }
}
