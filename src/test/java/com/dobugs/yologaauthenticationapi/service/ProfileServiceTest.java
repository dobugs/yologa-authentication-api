package com.dobugs.yologaauthenticationapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.domain.ResourceType;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.support.FakeStorageConnector;
import com.dobugs.yologaauthenticationapi.support.FakeStorageGenerator;
import com.dobugs.yologaauthenticationapi.support.StorageConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
@DisplayName("Profile 서비스 테스트")
class ProfileServiceTest {

    private static final Long MEMBER_ID = 0L;
    private static final String PROVIDER = Provider.GOOGLE.getName();
    private static final String ACCESS_TOKEN = "accessToken";

    private ProfileService profileService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    private StorageConnector fakeS3Connector;

    @BeforeEach
    void setUp() {
        fakeS3Connector = new FakeStorageConnector();
        profileService = new ProfileService(memberRepository, tokenGenerator, fakeS3Connector, new FakeStorageGenerator());
    }

    private String createToken(final Long memberId, final String provider, final String token) {
        return Jwts.builder()
            .claim("memberId", memberId)
            .claim("provider", provider)
            .claim("token", token)
            .compact();
    }

    @DisplayName("프로필 수정 테스트")
    @Nested
    public class update {

        private static final String RESOURCE_NAME = "profile.png";

        final MockMultipartFile newProfile = new MockMultipartFile(
            "profile",
            "최종_최종_최종_프로필.png",
            MediaType.IMAGE_PNG_VALUE,
            "new profile content".getBytes()
        );

        @DisplayName("프로필을 수정한다")
        @Test
        void success() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            final Member member = new Member("oauthId");
            member.updateProfile(new Resource(RESOURCE_NAME, ResourceType.PROFILE, "http://localhost:8080/profile.png"));
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            final Resource beforeResource = member.getResource();
            profileService.update(serviceToken, newProfile);
            final Resource afterResource = member.getResource();

            assertThat(afterResource).isNotEqualTo(beforeResource);
        }

        @DisplayName("기존에 프로필이 없었더라도 프로필 수정에 성공한다")
        @Test
        void profileIsNull() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            final Member member = new Member("oauthId");
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            profileService.update(serviceToken, newProfile);

            assertThat(member.getResource()).isNotNull();
        }

        @DisplayName("이미지 파일이 아닐 경우 예외가 발생한다")
        @Test
        void fileIsNotImage() {
            final MockMultipartFile newProfile = new MockMultipartFile(
                "profile",
                "최종_최종_최종_프로필.png",
                MediaType.APPLICATION_JSON_VALUE,
                "new profile content".getBytes()
            );

            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            assertThatThrownBy(() -> profileService.update(serviceToken, newProfile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("프로필은 PNG, JPG 형식만 가능합니다.");
        }

        @DisplayName("존재하지 않는 사용자의 프로필을 수정하면 예외가 발생한다")
        @Test
        void memberIsNotExist() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.update(serviceToken, newProfile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }

    @DisplayName("프로필 초기화 테스트")
    @Nested
    public class init {

        private static final String RESOURCE_NAME = "profile.png";

        private final MockMultipartFile resource = new MockMultipartFile(
            "profile",
            "최종_최종_최종_프로필.png",
            MediaType.IMAGE_PNG_VALUE,
            "new profile content".getBytes()
        );

        @BeforeEach
        void setUp() {
            fakeS3Connector.save(resource, "/", RESOURCE_NAME);
        }

        @DisplayName("프로필을 초기화한다")
        @Test
        void success() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            final Member member = new Member("oauthId");
            member.updateProfile(new Resource(RESOURCE_NAME, ResourceType.PROFILE, "http://localhost:8080/profile.png"));
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            profileService.init(serviceToken);

            assertThat(member.getResource()).isNull();
        }

        @DisplayName("기존에 프로필이 없었더라도 프로필 초기화에 성공한다")
        @Test
        void profileIsNull() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            final Member member = new Member("oauthId");
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            profileService.init(serviceToken);

            assertThat(member.getResource()).isNull();
        }

        @DisplayName("존재하지 않는 사용자의 프로필을 초기화하면 예외가 발생한다")
        @Test
        void memberIsNotExist() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.init(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }
}
