package com.dobugs.yologaauthenticationapi.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
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

import com.dobugs.yologaauthenticationapi.service.MemberService;
import com.dobugs.yologaauthenticationapi.service.dto.request.MemberUpdateRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WebMvcTest(MemberController.class)
@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
@DisplayName("Member 컨트롤러 테스트")
class MemberControllerTest {

    private static final String BASIC_URL = "/api/v1/members";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @DisplayName("사용자 아이디를 이용하여 사용자 정보를 조회한다")
    @Test
    void findById() throws Exception {
        final long memberId = 0L;

        final MemberResponse response = new MemberResponse(
            memberId, "0123456789", "유콩", "010-0000-0000",
            "https://lh3.googleusercontent.com/a/AEdFTp6-48aO-w67aAJcYb22G0BLTvY23z4uMBb1Nec=s96-c"
        );
        given(memberService.findById(memberId)).willReturn(response);

        mockMvc.perform(get(BASIC_URL + "/" + memberId))
            .andExpect(status().isOk())
            .andDo(document(
                "member/find-by-id",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("JWT 를 이용하여 내 정보를 조회한다")
    @Test
    void findMe() throws Exception {
        final String accessToken = "accessToken";

        final MemberResponse response = new MemberResponse(
            0L, "0123456789", "유콩", "010-0000-0000",
            "https://lh3.googleusercontent.com/a/AEdFTp6-48aO-w67aAJcYb22G0BLTvY23z4uMBb1Nec=s96-c"
        );
        given(memberService.findMe(accessToken)).willReturn(response);

        mockMvc.perform(get(BASIC_URL + "/me")
                .header("Authorization", accessToken))
            .andExpect(status().isOk())
            .andDo(document(
                "member/find-me",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("내 정보를 수정한다")
    @Test
    void update() throws Exception {
        final String accessToken = "accessToken";

        final MemberUpdateRequest request = new MemberUpdateRequest("유콩", "010-0000-0000");
        final String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(BASIC_URL)
                .header("Authorization", accessToken)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document(
                "member/update",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }

    @DisplayName("탈퇴한다")
    @Test
    void deleteMember() throws Exception {
        final String accessToken = "accessToken";

        mockMvc.perform(delete(BASIC_URL)
                .header("Authorization", accessToken))
            .andExpect(status().isOk())
            .andDo(document(
                "member/delete",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()))
            )
        ;
    }
}
