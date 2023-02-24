package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.multipart.MultipartFile;

public class FakeStorageGenerator implements StorageGenerator {

    @Override
    public String createPath() {
        return "/path";
    }

    @Override
    public String createResourceName(final MultipartFile resource) {
        return "profile.png";
    }
}
