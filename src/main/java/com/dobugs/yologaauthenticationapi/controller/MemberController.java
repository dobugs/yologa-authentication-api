package com.dobugs.yologaauthenticationapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dobugs.yologaauthenticationapi.auth.Authorized;
import com.dobugs.yologaauthenticationapi.auth.ExtractAuthorization;
import com.dobugs.yologaauthenticationapi.auth.dto.response.ServiceToken;
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
    public ResponseEntity<MemberResponse> findMe(@ExtractAuthorization final ServiceToken serviceToken) {
        final MemberResponse response = memberService.findMe(serviceToken);
        return ResponseEntity.ok(response);
    }

    @Authorized
    @PostMapping
    public ResponseEntity<Void> update(
        @ExtractAuthorization final ServiceToken serviceToken,
        @RequestBody final MemberUpdateRequest request
    ) {
        memberService.update(serviceToken, request);
        return ResponseEntity.ok().build();
    }

    @Authorized
    @DeleteMapping
    public ResponseEntity<Void> delete(@ExtractAuthorization final ServiceToken serviceToken) {
        memberService.delete(serviceToken);
        return ResponseEntity.ok().build();
    }
}
