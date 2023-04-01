package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.multipart.MultipartFile;

public interface StorageGenerator {

    String createPath();

    String createResourceName(MultipartFile resource);
}
