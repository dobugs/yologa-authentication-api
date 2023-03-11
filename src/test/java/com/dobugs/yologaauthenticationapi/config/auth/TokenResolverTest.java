package com.dobugs.yologaauthenticationapi.config.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;

@AutoConfigureMockMvc
@WebMvcTest(FakeController.class)
@DisplayName("Token 리졸버 테스트")
class TokenResolverTest {

    private static final String BASIC_URL = "/api";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenExtractor tokenExtractor;

    @MockBean
    private TokenRepository tokenRepository;

    @DisplayName("@ExtractAuthorization 어노테이션에 없는 경우 테스트")
    @Nested
    public class hasNotAnnotation {

        private static final String URL = BASIC_URL + "/hasNotAnnotation";

        @DisplayName("@ExtractAuthorization 어노테이션이 없을 경우 객체로 추출하지 않는다")
        @Test
        void success() throws Exception {
            mockMvc.perform(get(URL))
                .andExpect(status().isOk())
            ;
        }
    }

    @DisplayName("@ExtractAuthorization 어노테이션이 있는 경우 테스트")
    @Nested
    public class hasLoginUserAnnotation {

        private static final String URL = BASIC_URL + "/hasLoginUserAnnotation";

        @DisplayName("@ExtractAuthorization 어노테이션이 있을 경우 객체를 추출한다")
        @Test
        void success() throws Exception {
            given(tokenExtractor.extract(any())).willReturn(new ServiceToken(0L, "google", "Bearer", "token"));

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
    }
}
