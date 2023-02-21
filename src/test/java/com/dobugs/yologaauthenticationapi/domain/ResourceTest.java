package com.dobugs.yologaauthenticationapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Resource 도메인 테스트")
class ResourceTest {

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
