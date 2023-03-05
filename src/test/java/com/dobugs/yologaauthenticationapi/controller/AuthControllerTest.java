package com.dobugs.yologaauthenticationapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.dobugs.yologaauthenticationapi.service.AuthService;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.ServiceTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(AuthController.class)
@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
@DisplayName("Auth 컨트롤러 테스트")
class AuthControllerTest {

    private static final String BASIC_URL = "/api/v1/oauth2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @DisplayName("OAuth 로그인 URL 을 생성한다")
    @Test
    void generateOAuthUrl() throws Exception {
        final String redirectUrl = "redirectUrl";

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("provider", "google");
        params.add("redirect_url", redirectUrl);
        params.add("referrer", "referrer");

        final OAuthLinkResponse response = new OAuthLinkResponse(redirectUrl);
        given(authService.generateOAuthUrl(any())).willReturn(response);

        mockMvc.perform(get(BASIC_URL + "/login")
                .params(params))
            .andExpect(status().isOk())
            .andDo(document(
                "auth/generate-OAuth-url",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("OAuth 로그인 시 토큰을 요청한다")
    @Test
    void login() throws Exception {
        final String redirectUrl = "redirectUrl";

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("provider", "google");
        params.add("redirect_url", redirectUrl);
        params.add("referrer", "referrer");

        final OAuthCodeRequest request = new OAuthCodeRequest("authorizationCode");
        final String body = objectMapper.writeValueAsString(request);

        final ServiceTokenResponse response = new ServiceTokenResponse("accessToken", "refreshToken");
        given(authService.login(any(), any())).willReturn(response);

        mockMvc.perform(post(BASIC_URL + "/login")
                .params(params)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document(
                "auth/login",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("Access Token 을 재발급받는다")
    @Test
    void reissue() throws Exception {
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";

        final ServiceTokenResponse response = new ServiceTokenResponse(accessToken, refreshToken);
        given(authService.reissue(refreshToken)).willReturn(response);

        mockMvc.perform(post(BASIC_URL + "/reissue")
                .header("Authorization", refreshToken))
            .andExpect(status().isOk())
            .andDo(document(
                "auth/reissue",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("로그아웃한다")
    @Test
    void logout() throws Exception {
        final String accessToken = "accessToken";

        mockMvc.perform(post(BASIC_URL + "/logout")
                .header("Authorization", accessToken))
            .andExpect(status().isOk())
            .andDo(document(
                "auth/logout",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }
}
