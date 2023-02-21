package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.MemberUpdateRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;

    @Transactional(readOnly = true)
    public MemberResponse findById(final Long memberId) {
        final Member savedMember = findMemberById(memberId);
        return new MemberResponse(
            savedMember.getId(),
            savedMember.getOauthId(),
            savedMember.getNickname(),
            savedMember.getPhoneNumber(),
            String.valueOf(savedMember.getResource())
        );
    }

    @Transactional(readOnly = true)
    public MemberResponse findMe(final String serviceToken) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();
        return findById(memberId);
    }

    public void update(final String serviceToken, final MemberUpdateRequest request) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();
        final Member savedMember = findMemberById(memberId);
        savedMember.update(request.nickname(), request.phoneNumber());
    }

    public void delete(final String serviceToken) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();
        final Member savedMember = findMemberById(memberId);
        savedMember.delete();
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findByIdAndArchivedIsTrue(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 사용자입니다. [%d]", memberId)));
    }
}
