package com.dobugs.yologaauthenticationapi.support.logging;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.dto.response.SlackCommentResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.SlackThreadResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SlackLogger {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String url;
    private final String token;
    private final String channelId;

    public void alert(final String commentMessage, final String threadMessage) {
        final ResponseEntity<SlackCommentResponse> commentResponse = connectSlackForComment(commentMessage);
        if (commentResponse.getStatusCode().isError()) {
            System.out.printf("%s: 슬랙 에러 메시지 전송에 실패하였습니다.\n", commentResponse.getStatusCode());
            return;
        }
        final String commentId = commentResponse.getBody().ts();
        final ResponseEntity<SlackThreadResponse> threadResponse = connectSlackForThread(threadMessage, commentId);
        validateResponseIsSuccess(threadResponse);
    }

    private ResponseEntity<SlackCommentResponse> connectSlackForComment(final String message) {
        final String serializeMessage = serializeMessage(message);
        final String urlWithParams = generateCommentUrl(serializeMessage);
        final HttpEntity<MultiValueMap<Object, String>> headers = createAuthorizationHeaders();
        return REST_TEMPLATE.postForEntity(urlWithParams, headers, SlackCommentResponse.class);
    }

    private ResponseEntity<SlackThreadResponse> connectSlackForThread(final String message, final String commentId) {
        final String serializeMessage = serializeMessage(message);
        final String urlWithParams = generateThreadUrl(commentId, serializeMessage);
        final HttpEntity<MultiValueMap<Object, String>> headers = createAuthorizationHeaders();
        return REST_TEMPLATE.postForEntity(urlWithParams, headers, SlackThreadResponse.class);
    }

    private void validateResponseIsSuccess(final ResponseEntity<SlackThreadResponse> threadResponse) {
        if (threadResponse.getStatusCode().isError()) {
            System.out.printf("%s: 슬랙 에러 상세 메시지 전송에 실패하였습니다.\n", threadResponse.getStatusCode());
        }
    }

    private String generateCommentUrl(final String message) {
        return String.format("%s?channel=%s&text=%s", url, channelId, message);
    }

    private String generateThreadUrl(final String commentId, final String message) {
        return String.format("%s?channel=%s&thread_ts=%s&text=%s", url, channelId, commentId, message);
    }

    private HttpEntity<MultiValueMap<Object, String>> createAuthorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private String serializeMessage(final Object content) {
        try {
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (final JsonProcessingException e) {
            System.out.println("Slack 요청 메시지를 직렬화하는 과정에서 에러가 발생하였습니다.");
            return null;
        }
    }
}
