package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.repository.ResourceRepository;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProfileService {

    private final ResourceRepository resourceRepository;
    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;

    public void init(final String serviceToken) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();

        final Member savedMember = findMemberById(memberId);
        final Resource savedResource = savedMember.getResource();
        if (savedResource == null) {
            return;
        }
        savedMember.deleteProfile();
        savedResource.delete();
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findByIdAndArchivedIsTrue(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 사용자입니다. [%d]", memberId)));
    }
}
