package com.dobugs.yologaauthenticationapi.support.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

        private final MockMultipartFile resource = new MockMultipartFile(
            "profile",
            "최종_최종_최종_프로필.png",
            MediaType.IMAGE_PNG_VALUE,
            "new profile content".getBytes()
        );

        @DisplayName("리소스를 저장한다")
        @Test
        void success() throws IOException {
            final String path = "/";
            final String resourceName = "profile.png";

            final S3Resource s3Resource = mock(S3Resource.class);
            given(s3Resource.getURL()).willReturn(new URL("http", "localhost", 8080, resourceName));
            given(s3Template.store(eq(fakeStorageProvider.bucket()), eq(resourceName), any())).willReturn(s3Resource);

            final ResourceResponse response = s3Connector.save(resource, path, resourceName);

            assertAll(
                () -> assertThat(response.resourceKey()).isEqualTo(resourceName),
                () -> assertThat(response.resourceUrl()).contains(resourceName)
            );
        }

        @DisplayName("리소스 키 설정 테스트")
        @Nested
        public class concatResourceKey {

            private static final String RESOURCE_NAME = "profile.png";
            private static final String DIRECTORY_NAME = "path";

            private StorageConnector s3Connector;

            @BeforeEach
            void setUp() throws IOException {
                final S3Resource s3Resource = mock(S3Resource.class);
                final S3Template s3Template = mock(S3Template.class);
                given(s3Resource.getURL()).willReturn(new URL("https", "host", 1234, "file"));
                given(s3Template.store(any(), any(), any())).willReturn(s3Resource);

                s3Connector = new S3Connector(s3Template, new FakeStorageProvider());
            }

            @DisplayName("리소스를 루트 위치에 저장하면 리소스 키는 '리소스 이름' 이다")
            @ParameterizedTest
            @ValueSource(strings = {"", "/"})
            void pathIsEmpty(final String path) {
                final ResourceResponse response = s3Connector.save(resource, path, RESOURCE_NAME);

                assertThat(response.resourceKey()).isEqualTo(RESOURCE_NAME);
            }

            @DisplayName("리소스를 폴더에 저장하면 리소스 키는 '폴더/리소스이름' 이다")
            @ParameterizedTest
            @ValueSource(strings = {
                "path", "/path", "path/", "/path/",
                "path/path", "/path/path", "path/path/", "/path/path/"
            })
            void pathIsPresent(final String path) {
                final ResourceResponse response = s3Connector.save(resource, path, RESOURCE_NAME);

                assertAll(
                    () -> assertThat(response.resourceKey()).doesNotStartWith("/"),
                    () -> assertThat(response.resourceKey()).contains(DIRECTORY_NAME + "/" + RESOURCE_NAME)
                );
            }
        }
    }

    @DisplayName("리소스 삭제 테스트")
    @Nested
    public class delete {

        @DisplayName("리소스를 삭제한다")
        @Test
        void success() {
            final String resourceKey = "profile.png";

            assertThatCode(() -> s3Connector.delete(resourceKey))
                .doesNotThrowAnyException();
        }
    }
}
