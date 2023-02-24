package com.dobugs.yologaauthenticationapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.domain.ResourceType;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.support.StorageConnector;
import com.dobugs.yologaauthenticationapi.support.TokenGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.ResourceResponse;
import com.dobugs.yologaauthenticationapi.support.dto.response.UserTokenResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProfileService {

    private static final ResourceType PROFILE_TYPE = ResourceType.PROFILE;

    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;
    private final StorageConnector s3Connector;

    public String update(final String serviceToken, final MultipartFile newProfile) {
        final UserTokenResponse userTokenResponse = tokenGenerator.extract(serviceToken);
        final Long memberId = userTokenResponse.memberId();

        final Member savedMember = findMemberById(memberId);
        final Resource savedResource = savedMember.getResource();
        if (savedResource != null) {
            savedResource.delete();
            s3Connector.delete(savedResource.getResourceKey());
        }

        final ResourceResponse response = s3Connector.save(newProfile, "/", "profile.png");
        final Resource resource = new Resource(response.resourceKey(), PROFILE_TYPE, response.resourceUrl());
        savedMember.updateProfile(resource);
        return response.resourceUrl();
    }

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
        s3Connector.delete(savedResource.getResourceKey());
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findByIdAndArchivedIsTrue(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 사용자입니다. [%d]", memberId)));
    }
}
