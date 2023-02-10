package com.dobugs.yologaauthenticationapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dobugs.yologaauthenticationapi.service.AuthService;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class AuthController {

    private final AuthService authService;

    @GetMapping("/oauth2/login")
    public ResponseEntity<OAuthLinkResponse> generateOAuthUrl(@ModelAttribute final OAuthRequest request) {
        final OAuthLinkResponse response = authService.generateOAuthUrl(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth2/login")
    public ResponseEntity<OAuthTokenResponse> login(
        @ModelAttribute final OAuthRequest request,
        @RequestBody final OAuthCodeRequest codeRequest
    ) {
        final OAuthTokenResponse response = authService.login(request, codeRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth2/reissue")
    public ResponseEntity<OAuthTokenResponse> reissue(@RequestHeader("Authorization") final String refreshToken) {
        final OAuthTokenResponse response = authService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }
}
