package com.dobugs.yologaauthenticationapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Resource 도메인 테스트")
class ResourceTest {

    @DisplayName("리소스 객체 생성 테스트")
    @Nested
    public class create {

        @DisplayName("리소스 객체를 생성한다")
        @Test
        void success() {
            assertThatCode(() -> new Resource("resourceKey", ResourceType.PROFILE, "resourceUrl"))
                .doesNotThrowAnyException();
        }

        @DisplayName("리소스 키의 길이가 범위를 벗어나면 예외가 발생한다")
        @Test
        void resourceKeyLengthIsOverStandard() {
            final String resourceKey = "resourceKey".repeat(255);

            assertThatThrownBy(() -> new Resource(resourceKey, ResourceType.PROFILE, "resourceUrl"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리소스의 key 의 길이가")
                .hasMessageContaining("자 이상입니다.");
        }

        @DisplayName("리소스 URL 의 길이가 범위를 벗어나면 예외가 발생한다")
        @Test
        void resourceUrlLengthIsOverStandard() {
            final String resourceUrl = "resourceUrl".repeat(500);

            assertThatThrownBy(() -> new Resource("resourceKey", ResourceType.PROFILE, resourceUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리소스의 URL 의 길이가")
                .hasMessageContaining("자 이상입니다.");
        }
    }

    @DisplayName("리소스 삭제 테스트")
    @Nested
    public class delete {

        @DisplayName("리소스를 삭제한다")
        @Test
        void success() {
            final Resource resource = new Resource();

            resource.delete();

            assertThat(resource.isArchived()).isFalse();
        }
    }
}
