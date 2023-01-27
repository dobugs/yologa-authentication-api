package com.dobugs.yologaauthenticationapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Provider 도메인 테스트")
class ProviderTest {

    @DisplayName("Provider 조회 테스트")
    @Nested
    public class findOf {

        @DisplayName("google 을 조회한다")
        @ParameterizedTest
        @ValueSource(strings = {"google", "GOOGLE", "Google"})
        void findGoogle(String providerName) {
            final Provider provider = Provider.findOf(providerName);

            assertThat(provider).isEqualTo(Provider.GOOGLE);
        }

        @DisplayName("kakao 를 조회한다")
        @ParameterizedTest
        @ValueSource(strings = {"kakao", "KAKAO", "Kakao"})
        void findKakao(String providerName) {
            final Provider provider = Provider.findOf(providerName);

            assertThat(provider).isEqualTo(Provider.KAKAO);
        }
    }
}
