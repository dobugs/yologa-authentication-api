package com.dobugs.yologaauthenticationapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dobugs.yologaauthenticationapi.config.auth.Authorized;
import com.dobugs.yologaauthenticationapi.config.auth.ValidatedRefreshToken;
import com.dobugs.yologaauthenticationapi.service.AuthService;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthCodeRequest;
import com.dobugs.yologaauthenticationapi.service.dto.request.OAuthRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.OAuthLinkResponse;
import com.dobugs.yologaauthenticationapi.service.dto.response.ServiceTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
@RestController
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<OAuthLinkResponse> generateOAuthUrl(@ModelAttribute final OAuthRequest request) {
        final OAuthLinkResponse response = authService.generateOAuthUrl(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ServiceTokenResponse> login(
        @ModelAttribute final OAuthRequest request,
        @RequestBody final OAuthCodeRequest codeRequest
    ) {
        final ServiceTokenResponse response = authService.login(request, codeRequest);
        return ResponseEntity.ok(response);
    }

    @Authorized
    @ValidatedRefreshToken
    @PostMapping("/reissue")
    public ResponseEntity<ServiceTokenResponse> reissue(@RequestHeader("Authorization") final String refreshToken) {
        final ServiceTokenResponse response = authService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }

    @Authorized
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") final String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok().build();
    }
}
