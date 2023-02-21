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
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
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

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(memberRepository, tokenGenerator);
    }

    private String createToken(final Long memberId, final String provider, final String token) {
        return Jwts.builder()
            .claim("memberId", memberId)
            .claim("provider", provider)
            .claim("token", token)
            .compact();
    }

    @DisplayName("프로필 초기화 테스트")
    @Nested
    public class init {

        @DisplayName("프로필을 초기화한다")
        @Test
        void success() {
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

    @DisplayName("프로필 수정 테스트")
    @Nested
    public class update {

        @DisplayName("프로필을 수정한다")
        @Test
        void success() {
            final MultipartFile newProfile = mock(MultipartFile.class);
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            final Member member = new Member("oauthId");
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            final Resource beforeResource = member.getResource();
            profileService.update(serviceToken, newProfile);
            final Resource afterResource = member.getResource();

            assertThat(afterResource).isNotEqualTo(beforeResource);
        }

        @DisplayName("존재하지 않는 사용자의 프로필을 수정하면 예외가 발생한다")
        @Test
        void memberIsNotExist() {
            final MultipartFile newProfile = mock(MultipartFile.class);

            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> profileService.update(serviceToken, newProfile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }
}
