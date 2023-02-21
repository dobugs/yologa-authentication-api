package com.dobugs.yologaauthenticationapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dobugs.yologaauthenticationapi.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members/profile")
@RestController
public class ProfileController {

    private final ProfileService profileService;

    @DeleteMapping
    public ResponseEntity<Void> init(@RequestHeader("Authorization") final String accessToken) {
        profileService.init(accessToken);
        return ResponseEntity.ok().build();
    }
}
