package com.dobugs.yologaauthenticationapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.dobugs.yologaauthenticationapi.domain.Member;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@DisplayName("Member 레파지토리 테스트")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("OAuth ID 를 이용하여 Member 를 조회하는 테스트")
    @Nested
    public class findByOauthId {

        @DisplayName("해당 OAuth ID 를 가지는 Member 를 조회한다")
        @Test
        void exist() {
            final String oAuthId = "oauthID";
            final Member member = new Member(oAuthId);
            memberRepository.save(member);

            final Optional<Member> savedMember = memberRepository.findByOauthId(oAuthId);

            assertThat(savedMember).isPresent();
        }

        @DisplayName("해당 OAuth ID 를 가지지 않을 경우 Member 를 조회하지 못한다")
        @Test
        void notExist() {
            final String oAuthId = "oauthID";

            final Optional<Member> savedMember = memberRepository.findByOauthId(oAuthId);

            assertThat(savedMember).isEmpty();
        }
    }
}
