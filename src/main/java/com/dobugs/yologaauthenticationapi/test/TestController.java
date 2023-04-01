package com.dobugs.yologaauthenticationapi.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
@RestController
public class TestController {

    private final TestService testService;

    @PostMapping("/profiles")
    public ResponseEntity<Void> createProfiles(@RequestParam("count") final Integer count) {
        testService.createProfile(count);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/members")
    public ResponseEntity<Void> createMembers(
        @RequestParam("count") final Integer count,
        @RequestParam("resourceId") final Long resourceId
    ) {
        testService.createMember(count, resourceId);
        return ResponseEntity.ok().build();
    }
}
