package com.dobugs.yologaauthenticationapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dobugs.yologaauthenticationapi.config.auth.Authorized;
import com.dobugs.yologaauthenticationapi.service.MemberService;
import com.dobugs.yologaauthenticationapi.service.dto.request.MemberUpdateRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> findById(@PathVariable final Long memberId) {
        final MemberResponse response = memberService.findById(memberId);
        return ResponseEntity.ok(response);
    }

    @Authorized
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> findMe(@RequestHeader("Authorization") final String accessToken) {
        final MemberResponse response = memberService.findMe(accessToken);
        return ResponseEntity.ok(response);
    }

    @Authorized
    @PostMapping
    public ResponseEntity<Void> update(
        @RequestHeader("Authorization") final String accessToken,
        @RequestBody final MemberUpdateRequest request
    ) {
        memberService.update(accessToken, request);
        return ResponseEntity.ok().build();
    }

    @Authorized
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") final String accessToken) {
        memberService.delete(accessToken);
        return ResponseEntity.ok().build();
    }
}
