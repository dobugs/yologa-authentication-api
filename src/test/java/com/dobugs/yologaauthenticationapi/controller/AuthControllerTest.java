package com.dobugs.yologaauthenticationapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.dobugs.yologaauthenticationapi.service.AuthService;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(AuthController.class)
@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
@DisplayName("Auth 컨트롤러 테스트")
class AuthControllerTest {

    private static final String BASIC_URL = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @DisplayName("OAuth 로그인 URL 을 생성한다")
    @Test
    void generateOAuthUrl() throws Exception {
        final String redirectUrl = "redirectUrl";

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("provider", "google");
        params.add("redirect_url", redirectUrl);

        final OAuthLinkResponse response = new OAuthLinkResponse(redirectUrl);
        given(authService.generateOAuthUrl(any())).willReturn(response);

        mockMvc.perform(get(BASIC_URL + "/oauth2/login")
                .params(params))
            .andExpect(status().isOk())
            .andDo(document(
                "auth/generate-OAuth-url",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }
}
