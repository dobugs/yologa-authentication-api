package com.dobugs.yologaauthenticationapi.support;

import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.support.dto.response.ResourceResponse;

public interface StorageConnector {

    ResourceResponse save(MultipartFile resource, String path, String resourceName);

    void delete(String resourceKey);
}
