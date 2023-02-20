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
import com.dobugs.yologaauthenticationapi.domain.Provider;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member 서비스 테스트")
class MemberServiceTest {

    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, tokenGenerator);
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
            given(memberRepository.findById(memberId)).willReturn(savedMember);

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
        private static final String PROVIDER = Provider.GOOGLE.getName();
        private static final String ACCESS_TOKEN = "accessToken";

        @DisplayName("JWT 를 이용하여 사용자 정보를 조회한다")
        @Test
        void success() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            final Member member = mock(Member.class);
            final Optional<Member> savedMember = Optional.of(member);
            given(member.getId()).willReturn(MEMBER_ID);
            given(memberRepository.findById(MEMBER_ID)).willReturn(savedMember);

            final MemberResponse response = memberService.findMe(serviceToken);
            assertThat(response.id()).isEqualTo(MEMBER_ID);
        }

        @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
        @Test
        void fail() {
            final String serviceToken = createToken(MEMBER_ID, PROVIDER, ACCESS_TOKEN);
            given(tokenGenerator.extract(serviceToken)).willReturn(new UserTokenResponse(MEMBER_ID, PROVIDER, ACCESS_TOKEN));

            assertThatThrownBy(() -> memberService.findMe(serviceToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
        }

        private String createToken(final Long memberId, final String provider, final String token) {
            return Jwts.builder()
                .claim("memberId", memberId)
                .claim("provider", provider)
                .claim("token", token)
                .compact();
        }
    }
}
