package com.dobugs.yologaauthenticationapi.service;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.config.dto.response.ServiceToken;
import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.domain.ResourceType;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.support.StorageConnector;
import com.dobugs.yologaauthenticationapi.support.StorageGenerator;
import com.dobugs.yologaauthenticationapi.support.dto.response.ResourceResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProfileService {

    private static final ResourceType PROFILE_TYPE = ResourceType.PROFILE;
    private static final List<String> CONTENT_TYPES_OF_IMAGE = List.of(MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE);

    private final MemberRepository memberRepository;
    private final StorageConnector s3Connector;
    private final StorageGenerator s3Generator;

    public String update(final ServiceToken serviceToken, final MultipartFile newProfile) {
        validateProfileIsImage(newProfile);

        final Member savedMember = findMemberById(serviceToken.memberId());
        final Resource savedResource = savedMember.getResource();
        if (savedResource != null) {
            savedResource.delete();
            s3Connector.delete(savedResource.getResourceKey());
        }

        final ResourceResponse response = s3Connector.save(newProfile, s3Generator.createPath(), s3Generator.createResourceName(newProfile));
        final Resource resource = new Resource(response.resourceKey(), PROFILE_TYPE, response.resourceUrl());
        savedMember.updateProfile(resource);
        return response.resourceUrl();
    }

    public void init(final ServiceToken serviceToken) {
        final Member savedMember = findMemberById(serviceToken.memberId());
        final Resource savedResource = savedMember.getResource();
        if (savedResource == null) {
            return;
        }
        savedMember.deleteProfile();
        savedResource.delete();
        s3Connector.delete(savedResource.getResourceKey());
    }

    private void validateProfileIsImage(final MultipartFile file) {
        final String contentType = file.getContentType();
        if (!CONTENT_TYPES_OF_IMAGE.contains(contentType)) {
            throw new IllegalArgumentException(String.format("프로필은 PNG, JPG 형식만 가능합니다. [%s]", contentType));
        }
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findByIdAndArchivedIsTrue(memberId)
            .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 사용자입니다. [%d]", memberId)));
    }
}
