package com.dobugs.yologaauthenticationapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
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

import com.dobugs.yologaauthenticationapi.auth.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.MemberUpdateRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;
import com.dobugs.yologaauthenticationapi.support.fixture.ServiceTokenFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member 서비스 테스트")
class MemberServiceTest {

    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @DisplayName("사용자 정보 조회 테스트")
    @Nested
    public class findById {

        @DisplayName("사용자의 아이디를 이용하여 사용자 정보를 조회한다")
        @Test
        void success() {
            final long memberId = 0L;

            final Member member = mock(Member.class);
            final Optional<Member> savedMember = Optional.of(member);
            given(member.getId()).willReturn(memberId);
            given(memberRepository.findByIdAndArchivedIsTrue(memberId)).willReturn(savedMember);

            final MemberResponse response = memberService.findById(memberId);
            assertThat(response.id()).isEqualTo(memberId);
        }

        @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
        @Test
        void fail() {
            final long memberId = 0L;

            assertThatThrownBy(() -> memberService.findById(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }

    @DisplayName("내 정보 조회 테스트")
    @Nested
    public class findMe {

        private static final Long MEMBER_ID = 0L;

        private ServiceToken serviceToken;

        @BeforeEach
        void setUp() {
            serviceToken = new ServiceTokenFixture.Builder()
                .memberId(MEMBER_ID)
                .provider(Provider.GOOGLE.getName())
                .tokenType("Bearer")
                .token("accessToken")
                .build();
        }

        @DisplayName("JWT 를 이용하여 사용자 정보를 조회한다")
        @Test
        void success() {
            final Member member = mock(Member.class);
            final Optional<Member> savedMember = Optional.of(member);
            given(member.getId()).willReturn(MEMBER_ID);
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(savedMember);

            final MemberResponse response = memberService.findMe(serviceToken);
            assertThat(response.id()).isEqualTo(MEMBER_ID);
        }

        @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
        @Test
        void fail() {
            assertThatThrownBy(() -> memberService.findMe(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }

    @DisplayName("내 정보 수정 테스트")
    @Nested
    public class update {

        private static final Long MEMBER_ID = 0L;
        private static final String NICKNAME = "유콩";
        private static final String PHONE_NUMBER = "010-0000-0000";

        private ServiceToken serviceToken;
        private MemberUpdateRequest request;

        @BeforeEach
        void setUp() {
            serviceToken = new ServiceTokenFixture.Builder()
                .memberId(MEMBER_ID)
                .provider(Provider.GOOGLE.getName())
                .tokenType("Bearer")
                .token("accessToken")
                .build();
            request = new MemberUpdateRequest(NICKNAME, PHONE_NUMBER);
        }

        @DisplayName("내 정보를 수정한다")
        @Test
        void success() {
            final Member member = new Member("oauthId");
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            memberService.update(serviceToken, request);

            assertAll(
                () -> assertThat(member.getNickname()).isEqualTo(NICKNAME),
                () -> assertThat(member.getPhoneNumber()).isEqualTo(PHONE_NUMBER)
            );
        }

        @DisplayName("존재하지 않는 사용자 정보를 수정하면 예외가 발생한다")
        @Test
        void fail() {
            assertThatThrownBy(() -> memberService.update(serviceToken, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }

    @DisplayName("탈퇴하기 테스트")
    @Nested
    public class delete {

        private static final Long MEMBER_ID = 0L;

        private ServiceToken serviceToken;

        @BeforeEach
        void setUp() {
            serviceToken = new ServiceTokenFixture.Builder()
                .memberId(MEMBER_ID)
                .provider(Provider.GOOGLE.getName())
                .tokenType("Bearer")
                .token("accessToken")
                .build();
        }

        @DisplayName("탈퇴한다")
        @Test
        void success() {
            final Member member = new Member("oauthId");
            given(memberRepository.findByIdAndArchivedIsTrue(MEMBER_ID)).willReturn(Optional.of(member));

            memberService.delete(serviceToken);

            assertThat(member.isArchived()).isFalse();
        }

        @DisplayName("존재하지 않는 사용자가 탈퇴하면 예외가 발생한다")
        @Test
        void fail() {
            assertThatThrownBy(() -> memberService.delete(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }
    }
}
