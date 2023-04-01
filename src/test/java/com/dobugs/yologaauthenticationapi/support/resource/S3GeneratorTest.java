package com.dobugs.yologaauthenticationapi.support.resource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.dobugs.yologaauthenticationapi.support.StorageGenerator;

@DisplayName("S3Generator 테스트")
class S3GeneratorTest {

    private StorageGenerator s3Generator;

    @BeforeEach
    void setUp() {
        s3Generator = new S3Generator();
    }

    @DisplayName("리소스의 이름을 생성한다")
    @Test
    void createResourceName() {
        final String extension = "png";
        final MockMultipartFile resource = new MockMultipartFile(
            "profile",
            "최종_최종_최종_프로필." + extension,
            MediaType.IMAGE_PNG_VALUE,
            "new profile content".getBytes()
        );

        final String resourceName = s3Generator.createResourceName(resource);

        assertThat(resourceName).endsWith("." + extension);
    }
}
