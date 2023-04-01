package com.dobugs.yologaauthenticationapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.dobugs.yologaauthenticationapi.service.ProfileService;

@WebMvcTest(ProfileController.class)
@DisplayName("profile 컨트롤러 테스트")
class ProfileControllerTest extends ControllerTest {

    private static final String BASIC_URL = "/api/v1/members/profile";

    @MockBean
    private ProfileService profileService;

    @DisplayName("프로필을 수정한다")
    @Test
    void update() throws Exception {
        final MockMultipartFile newProfile = new MockMultipartFile(
            "profile",
            "최종_최종_최종_프로필.png",
            MediaType.IMAGE_PNG_VALUE,
            "new profile content".getBytes()
        );

        given(profileService.update(any(), eq(newProfile))).willReturn("profileUrl");

        mockMvc.perform(multipart(BASIC_URL).file(newProfile)
                .header("Authorization", "accessToken"))
            .andExpect(status().isCreated())
            .andDo(document(
                "profile/update",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("프로필을 초기화한다")
    @Test
    void init() throws Exception {
        final String accessToken = "accessToken";

        mockMvc.perform(delete(BASIC_URL)
                .header("Authorization", accessToken))
            .andExpect(status().isOk())
            .andDo(document(
                "profile/init",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }
}
