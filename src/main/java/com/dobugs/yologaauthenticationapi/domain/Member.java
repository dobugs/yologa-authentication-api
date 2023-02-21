package com.dobugs.yologaauthenticationapi.domain;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Member extends BaseEntity {

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{3}-\\d{3,4}-\\d{4}$");
    private static final int NICKNAME_LENGTH = 50;
    private static final int PHONE_NUMBER_LENGTH = 50;
    private static final String INITIAL_NICKNAME_PREFIX = "DOBUGS#";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String oauthId;

    @Column(length = NICKNAME_LENGTH)
    private String nickname;

    @Column(length = PHONE_NUMBER_LENGTH)
    private String phoneNumber;

    @OneToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    public Member(final String oauthId) {
        this.oauthId = oauthId;
    }

    public void init() {
        nickname = INITIAL_NICKNAME_PREFIX + id;
    }

    public void update(final String nickname, final String phoneNumber) {
        validateNickname(nickname);
        validatePhoneNumber(phoneNumber);
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    public void delete() {
        deleteEntity();
    }

    public void updateProfile(final Resource resource) {
        this.resource = resource;
    }

    public void deleteProfile() {
        this.resource = null;
    }

    private void validateNickname(final String nickname) {
        if (nickname.length() > NICKNAME_LENGTH) {
            throw new IllegalArgumentException(String.format("닉네임은 %d자 이하여야 합니다. [%s]", NICKNAME_LENGTH, nickname));
        }
    }

    private void validatePhoneNumber(final String phoneNumber) {
        if (!PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException(String.format("올바른 전화번호 형식이 아닙니다. [%s]", phoneNumber));
        }
    }
}
