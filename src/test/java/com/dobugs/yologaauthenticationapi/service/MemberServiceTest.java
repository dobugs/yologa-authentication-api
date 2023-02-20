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

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;

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

    @DisplayName("사용자 아이디를 이용하여 사용자 정보 조회 테스트")
    @Nested
    public class findById {

        @DisplayName("사용자의 아이디를 이용하여 사용자 정보를 조회한다")
        @Test
        void success() {
            final long memberId = 0L;

            final Member member = mock(Member.class);
            given(member.getId()).willReturn(memberId);

            final Optional<Member> savedMember = Optional.of(member);
            given(memberRepository.findById(memberId))
                .willReturn(savedMember);

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
}
