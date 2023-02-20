package com.dobugs.yologaauthenticationapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dobugs.yologaauthenticationapi.service.MemberService;
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
}
