package com.dobugs.yologaauthenticationapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Resource extends BaseEntity {

    private static final int RESOURCE_KEY_LENGTH = 255;
    private static final int RESOURCE_URL_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String resourceKey;

    @Column(nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private ResourceType resourceType;

    @Column(nullable = false, length = RESOURCE_URL_LENGTH)
    private String resourceUrl;

    public Resource(final String resourceKey, final ResourceType resourceType, final String resourceUrl) {
        validateResourceKey(resourceKey);
        validateResourceUrl(resourceUrl);
        this.resourceKey = resourceKey;
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
    }

    private void validateResourceKey(final String resourceKey) {
        if (resourceKey.length() > RESOURCE_KEY_LENGTH) {
            throw new IllegalArgumentException(String.format("리소스의 key 의 길이가 %d자 이상입니다. [%s]", RESOURCE_KEY_LENGTH, resourceKey));
        }
    }

    private void validateResourceUrl(final String resourceUrl) {
        if (resourceUrl.length() > RESOURCE_URL_LENGTH) {
            throw new IllegalArgumentException(String.format("리소스의 URL 의 길이가 %d자 이상입니다. [%s]", RESOURCE_URL_LENGTH, resourceUrl));
        }
    }

    public void delete() {
        deleteEntity();
    }
}
