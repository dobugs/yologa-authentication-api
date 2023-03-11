package com.dobugs.yologaauthenticationapi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dobugs.yologaauthenticationapi.config.auth.TokenExtractor;
import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
@DisplayName("Auth 컨트롤러 테스트")
class AuthControllerTest {

    private static final String BASIC_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenExtractor tokenExtractor;

    @MockBean
    private TokenRepository tokenRepository;

    @DisplayName("JWT 검증 테스트")
    @Nested
    public class validate {

        private static final String URL = BASIC_URL + "/login";

        @DisplayName("JWT 를 검증한다")
        @Test
        void success() throws Exception {
            given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", "token"));
            given(tokenRepository.findRefreshToken(any())).willReturn(Optional.of("refreshToken"));

            mockMvc.perform(post(URL)
                    .header("Authorization", "token"))
                .andExpect(status().isOk())
            ;
        }

        @DisplayName("Authorization 헤더에 JWT 가 없을 경우 예외가 발생한다")
        @Test
        void notExistAuthorizationHeader() throws Exception {
            mockMvc.perform(post(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("토큰이 필요합니다.")))
            ;
        }

        @DisplayName("Redis 에 Refresh Token 이 없을 경우 예외가 발생한다")
        @Test
        void notExistRefreshToken() throws Exception {
            given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", "token"));
            given(tokenRepository.findRefreshToken(any())).willReturn(Optional.empty());

            mockMvc.perform(post(URL)
                    .header("Authorization", "token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("로그인이 필요한 서비스입니다.")))
            ;
        }
    }
}
