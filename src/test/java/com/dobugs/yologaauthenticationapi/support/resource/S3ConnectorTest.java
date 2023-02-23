package com.dobugs.yologaauthenticationapi.support.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.dobugs.yologaauthenticationapi.support.FakeStorageProvider;
import com.dobugs.yologaauthenticationapi.support.StorageConnector;
import com.dobugs.yologaauthenticationapi.support.StorageProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.ResourceResponse;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Connector 테스트")
class S3ConnectorTest {

    private static final String PROTOCOL = "http";
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private StorageConnector s3Connector;

    private StorageProvider fakeStorageProvider;

    @Mock
    private S3Template s3Template;

    @BeforeEach
    void setUp() {
        fakeStorageProvider = new FakeStorageProvider();
        s3Connector = new S3Connector(s3Template, fakeStorageProvider);
    }

    @DisplayName("리소스 저장 테스트")
    @Nested
    public class save {

        private static final String PATH = "/";
        private static final String RESOURCE_NAME = "profile.png";
        private static final String RESOURCE_KEY = PATH + RESOURCE_NAME;

        private final MockMultipartFile resource = new MockMultipartFile(
            "profile",
            "최종_최종_최종_프로필.png",
            MediaType.IMAGE_PNG_VALUE,
            "new profile content".getBytes()
        );

        @DisplayName("리소스를 저장한다")
        @Test
        void success() throws IOException {
            final S3Resource s3Resource = mock(S3Resource.class);
            given(s3Resource.getURL()).willReturn(new URL(PROTOCOL, HOST, PORT, RESOURCE_KEY));
            given(s3Template.store(fakeStorageProvider.bucket(), RESOURCE_KEY, resource)).willReturn(s3Resource);

            final ResourceResponse response = s3Connector.save(resource, PATH, RESOURCE_NAME);

            assertAll(
                () -> assertThat(response.resourceKey()).isEqualTo(RESOURCE_KEY),
                () -> assertThat(response.resourceUrl()).isEqualTo(PROTOCOL + "://" + HOST + ":" + PORT + RESOURCE_KEY)
            );
        }
    }

    @DisplayName("리소스 삭제 테스트")
    @Nested
    public class delete {

        @DisplayName("리소스를 삭제한다")
        @Test
        void success() {
            assertThatCode(() -> s3Connector.delete("resourceUrl"))
                .doesNotThrowAnyException();
        }
    }
}
