package com.dobugs.yologaauthenticationapi.test;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dobugs.yologaauthenticationapi.domain.Member;
import com.dobugs.yologaauthenticationapi.domain.Resource;
import com.dobugs.yologaauthenticationapi.domain.ResourceType;
import com.dobugs.yologaauthenticationapi.repository.MemberRepository;
import com.dobugs.yologaauthenticationapi.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class TestService {

    private static final int MAXIMUM_PROFILE_COUNT = 100;
    private static final int MAXIMUM_MEMBER_COUNT = 1_000;

    private final ResourceRepository resourceRepository;
    private final MemberRepository memberRepository;

    /**
     * 입력한 count 수만큼 더미 프로필 정보를 저장한다.
     */
    public void createProfile(final Integer count) {
        final String resourceKey = "member/profile/IMG_2831.JPG";
        final ResourceType resourceType = ResourceType.PROFILE;
        final String resourceUrl = "https://yologa-dev-image.s3.ap-northeast-2.amazonaws.com/member/profile/IMG_2831.JPG";

        if (count == null) {
            throw new IllegalArgumentException("count 를 입력해주세요.");
        }
        if (count > MAXIMUM_PROFILE_COUNT) {
            throw new IllegalArgumentException(String.format("count 는 %d 개 이하만 가능합니다.", MAXIMUM_PROFILE_COUNT));
        }

        for (int i = 0; i < count; i++) {
            resourceRepository.save(new Resource(resourceKey, resourceType, resourceUrl));
        }
    }

    /**
     * 입력한 count 수만큼 resourceId 를 가지는 더미 사용자 정보를 저장한다.
     */
    public void createMember(final Integer count, final Long resourceId) {
        if (count == null || resourceId == null) {
            throw new IllegalArgumentException("count 와 resourceId 를 입력해주세요.");
        }
        if (count > MAXIMUM_MEMBER_COUNT) {
            throw new IllegalArgumentException(String.format("count 는 %d 개 이하만 가능합니다.", MAXIMUM_MEMBER_COUNT));
        }

        final Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 resource 입니다."));
        for (int i = 0; i < count; i++) {
            final String oauthId = UUID.randomUUID().toString().replace("-", "");
            final Member member = new Member(oauthId);
            memberRepository.save(member);
            member.init();
            member.updateProfile(resource);
        }
    }
}
