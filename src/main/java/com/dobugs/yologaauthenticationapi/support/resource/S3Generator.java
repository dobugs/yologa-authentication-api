package com.dobugs.yologaauthenticationapi.support.resource;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.support.StorageGenerator;

@Component
public class S3Generator implements StorageGenerator {

    private static final String DEFAULT_PROFILE_PATH = "/member/profile";

    @Override
    public String createPath() {
        return DEFAULT_PROFILE_PATH;
    }

    @Override
    public String createResourceName(final MultipartFile resource) {
        final String filename = resource.getOriginalFilename();
        final String extension = filename.substring(filename.lastIndexOf(".") + 1);
        final String resourceName = UUID.randomUUID().toString().replace("-", "");
        return String.join(".", resourceName, extension);
    }
}
