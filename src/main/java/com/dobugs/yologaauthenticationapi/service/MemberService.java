package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;

    public MemberResponse findById(final Long memberId) {
        final Member savedMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 사용자입니다. [%d]", memberId)));
        return new MemberResponse(
            savedMember.getId(),
            savedMember.getOauthId(),
            savedMember.getNickname(),
            savedMember.getPhoneNumber(),
            String.valueOf(savedMember.getResourceId())
        );
    }

    public MemberResponse findMe(final String serviceToken) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();
        return findById(memberId);
    }
}