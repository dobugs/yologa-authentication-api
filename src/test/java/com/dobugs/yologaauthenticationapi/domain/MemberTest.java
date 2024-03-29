package com.dobugs.yologaauthenticationapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Member 도메인 테스트")
class MemberTest {

    @DisplayName("사용자 초기화 테스트")
    @Nested
    public class init {

        @DisplayName("사용자 정보를 초기화한다")
        @Test
        void success() {
            final Member member = new Member("oauthId");

            member.init();

            assertThat(member.getNickname()).contains("DOBUGS#");
        }
    }

    @DisplayName("사용자 재가입 테스트")
    @Nested
    public class rejoin {

        @DisplayName("사용자 정보를 재가입시킨다")
        @Test
        void success() {
            final Member member = new Member("oauthId");
            member.delete();

            member.rejoin();

            assertThat(member.isArchived()).isTrue();
        }
    }

    @DisplayName("사용자 수정 테스트")
    @Nested
    public class update {

        private Member member;

        @BeforeEach
        void setUp() {
            member = new Member("oauthId");
        }

        @DisplayName("사용자 정보를 수정한다")
        @Test
        void success() {
            final String nickname = "유콩";
            final String phoneNumber = "010-0000-0000";

            member.update(nickname, phoneNumber);

            assertAll(
                () -> assertThat(member.getNickname()).isEqualTo(nickname),
                () -> assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber)
            );
        }

        @DisplayName("닉네임이 50자 이상일 경우 예외가 발생한다")
        @Test
        void nicknameLengthIsOver50() {
            final String invalidNickname = "유콩".repeat(50);
            final String phoneNumber = "010-0000-0000";

            assertThatThrownBy(() -> member.update(invalidNickname, phoneNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은")
                .hasMessageContaining("자 이하여야 합니다.");
        }

        @DisplayName("전화번호가 올바른 형식이 아닐 경우 예외가 발생한다")
        @Test
        void phoneNumberFormatIsNotValid() {
            final String nickname = "유콩";
            final String invalidPhoneNumber = "0123456789";

            assertThatThrownBy(() -> member.update(nickname, invalidPhoneNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바른 전화번호 형식이 아닙니다.");
        }
    }

    @DisplayName("사용자 삭제 테스트")
    @Nested
    public class delete {

        @DisplayName("사용자를 삭제한다")
        @Test
        void success() {
            final Member member = new Member("oauthId");

            member.delete();

            assertThat(member.isArchived()).isFalse();
        }
    }

    @DisplayName("사용자 프로필 수정 테스트")
    @Nested
    public class updateProfile {

        @DisplayName("사용자 프로필을 수정한다")
        @Test
        void success() {
            final Member member = new Member("oauthId");
            final Resource resource = new Resource("resourceKey", ResourceType.PROFILE, "resourceUrl");

            member.updateProfile(resource);

            assertThat(member.getResource()).isEqualTo(resource);
        }
    }

    @DisplayName("사용자 프로필 삭제 테스트")
    @Nested
    public class deleteProfile {

        @DisplayName("사용자 프로필을 삭제한다")
        @Test
        void success() {
            final Member member = new Member("oauthId");

            member.deleteProfile();

            assertThat(member.getResource()).isNull();
        }
    }
}
