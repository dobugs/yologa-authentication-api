package com.dobugs.yologaauthenticationapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.dobugs.yologaauthenticationapi.config.auth.TokenExtractor;
import com.dobugs.yologaauthenticationapi.repository.TokenRepository;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
public class ControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private TokenExtractor tokenExtractor;

    @MockBean
    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        final String token = "token";

        given(tokenExtractor.extract(any())).willReturn(new UserTokenResponse(0L, "google", "Bearer", token));
        given(tokenRepository.findRefreshToken(any())).willReturn(Optional.of(token));
    }
}
