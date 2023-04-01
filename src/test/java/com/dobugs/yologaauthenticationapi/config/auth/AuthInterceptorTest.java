package com.dobugs.yologaauthenticationapi.config.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dobugs.yologaauthenticationapi.auth.TokenExtractor;
import com.dobugs.yologaauthenticationapi.auth.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;

@AutoConfigureMockMvc
@WebMvcTest(FakeController.class)
@DisplayName("Auth 인터셉터 테스트")
class AuthInterceptorTest {

    private static final String BASIC_URL = "/api";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenExtractor tokenExtractor;

    @MockBean
    private TokenRepository tokenRepository;

    @DisplayName("@Authorized 어노테이션이 없는 경우 테스트")
    @Nested
    public class hasNotAuthorizedAnnotation {

        private static final String URL = BASIC_URL + "/hasNotAnnotation";

        @DisplayName("@Authorized 어노테이션이 없을 경우 JWT 를 검증하지 않는다")
        @Test
        void success() throws Exception {
            mockMvc.perform(get(URL))
                .andExpect(status().isOk())
            ;
        }
    }

    @DisplayName("@Authorized 어노테이션이 있는 경우 테스트")
    @Nested
    public class hasAuthorizedAnnotation {

        private static final String URL = BASIC_URL + "/hasAuthorizedAnnotation";

        @DisplayName("@Authorized 어노테이션이 있을 경우 JWT 를 검증한다")
        @Test
        void success() throws Exception {
            given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", "token"));
            given(tokenRepository.findRefreshToken(any())).willReturn(Optional.of("refreshToken"));

            mockMvc.perform(get(URL)
                    .header("Authorization", "token"))
                .andExpect(status().isOk())
            ;
        }

        @DisplayName("Authorization 헤더에 JWT 가 없을 경우 예외가 발생한다")
        @Test
        void notExistAuthorizationHeader() throws Exception {
            mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("토큰이 필요합니다.")))
            ;
        }

        @DisplayName("Redis 에 Refresh Token 이 없을 경우 예외가 발생한다")
        @Test
        void notExistRefreshToken() throws Exception {
            given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", "token"));
            given(tokenRepository.findRefreshToken(any())).willReturn(Optional.empty());

            mockMvc.perform(get(URL)
                    .header("Authorization", "token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("로그인이 필요한 서비스입니다.")))
            ;
        }

        @DisplayName("@ValidatedRefreshToken 어노테이션이 있는 경우 테스트")
        @Nested
        public class hasValidatedRefreshTokenAnnotation {

            private static final String URL = BASIC_URL + "/hasValidatedRefreshTokenAnnotation";

            @DisplayName("@ValidatedRefreshToken 어노테이션이 있을 경우 저장된 Refresh Token 과 일치하는지 확인한다")
            @Test
            void success() throws Exception {
                final String refreshToken = "refreshToken";

                given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", refreshToken));
                given(tokenRepository.findRefreshToken(any())).willReturn(Optional.of(refreshToken));

                mockMvc.perform(get(URL)
                        .header("Authorization", "token"))
                    .andExpect(status().isOk())
                ;
            }

            @DisplayName("저장된 Refresh Token 과 일치하지 않으면 예외가 발생한다")
            @Test
            void notEqualRefreshToken() throws Exception {
                final String refreshToken = "refreshToken";
                final String savedRefreshToken = "savedRefreshToken";

                given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", refreshToken));
                given(tokenRepository.findRefreshToken(any())).willReturn(Optional.of(savedRefreshToken));

                mockMvc.perform(get(URL)
                        .header("Authorization", "token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("잘못된 refresh token 입니다.")))
                ;
            }
        }
    }
}
