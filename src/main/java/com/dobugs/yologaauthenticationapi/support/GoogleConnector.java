package com.dobugs.yologaauthenticationapi.support;

import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dobugs.yologaauthenticationapi.support.dto.response.GoogleTokenResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Component
public class GoogleConnector {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private final GoogleProvider provider;

    public String requestAccessToken(final String authorizationCode, final String redirectUrl) {
        final GoogleTokenResponse response = connectGoogle(authorizationCode, redirectUrl);
        return response.accessToken();
    }

    private GoogleTokenResponse connectGoogle(final String authorizationCode, final String redirectUrl) {
        final ResponseEntity<GoogleTokenResponse> googleResponse = REST_TEMPLATE.postForEntity(
            provider.getAccessTokenUrl(),
            provider.createEntity(authorizationCode, redirectUrl),
            GoogleTokenResponse.class
        );
        validateGoogleResponseIsSuccess(googleResponse);
        return Optional.ofNullable(googleResponse.getBody())
            .orElseThrow(() -> new IllegalArgumentException("Google 과의 연결에 실패하였습니다."));
    }

    private void validateGoogleResponseIsSuccess(final ResponseEntity<GoogleTokenResponse> googleResponse) {
        final HttpStatusCode statusCode = googleResponse.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("Google 과의 연결에 실패하였습니다. [%s]", statusCode));
        }
    }
}
