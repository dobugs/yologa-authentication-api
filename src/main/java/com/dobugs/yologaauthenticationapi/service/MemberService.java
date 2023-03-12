package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.auth.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.service.dto.request.MemberUpdateRequest;
import com.dobugs.yologaauthenticationapi.service.dto.response.MemberResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberResponse findById(final Long memberId) {
        final Member savedMember = findMemberById(memberId);
        final String resourceUrl = getResourceUrl(savedMember);
        return new MemberResponse(
            savedMember.getId(),
            savedMember.getOauthId(),
            savedMember.getNickname(),
            savedMember.getPhoneNumber(),
            resourceUrl
        );
    }

    @Transactional(readOnly = true)
    public MemberResponse findMe(final ServiceToken serviceToken) {
        return findById(serviceToken.memberId());
    }

    public void update(final ServiceToken serviceToken, final MemberUpdateRequest request) {
        final Member savedMember = findMemberById(serviceToken.memberId());
        savedMember.update(request.nickname(), request.phoneNumber());
    }

    public void delete(final ServiceToken serviceToken) {
        final Member savedMember = findMemberById(serviceToken.memberId());
        savedMember.delete();
    }

    private String getResourceUrl(final Member savedMember) {
        final Resource savedResource = savedMember.getResource();
        if (savedResource == null) {
            return null;
        }
        return savedResource.getResourceUrl();
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findByIdAndArchivedIsTrue(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 사용자입니다. [%d]", memberId)));
    }
}
