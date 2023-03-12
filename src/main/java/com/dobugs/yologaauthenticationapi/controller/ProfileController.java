package com.dobugs.yologaauthenticationapi.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.auth.Authorized;
import com.dobugs.yologaauthenticationapi.auth.ExtractAuthorization;
import com.dobugs.yologaauthenticationapi.auth.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members/profile")
@RestController
public class ProfileController {

    private final ProfileService profileService;

    @Authorized
    @PostMapping
    public ResponseEntity<Void> update(
        @ExtractAuthorization final ServiceToken serviceToken,
        @RequestParam("profile") final MultipartFile newProfile
    ) {
        final String profileUrl = profileService.update(serviceToken, newProfile);
        return ResponseEntity.created(URI.create(profileUrl)).build();
    }

    @Authorized
    @DeleteMapping
    public ResponseEntity<Void> init(@ExtractAuthorization final ServiceToken serviceToken) {
        profileService.init(serviceToken);
        return ResponseEntity.ok().build();
    }
}
